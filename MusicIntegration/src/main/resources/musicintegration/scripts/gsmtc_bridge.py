import sys
import json
import asyncio
import base64
import time
from ctypes import cast, POINTER
from comtypes import CLSCTX_ALL
from pycaw.pycaw import AudioUtilities, IAudioMeterInformation
import winsdk.windows.media.control as wmc

sys.stdin.reconfigure(encoding='utf-8')
sys.stdout.reconfigure(encoding='utf-8', line_buffering=True)

# ── Audio peak meter setup ────────────────────────────────────────────────────
_peak_meter = None

def get_peak(app_id: str, is_playing: bool) -> float:
    """Return peak audio level 0.0-1.0."""
    # Always read peak — state from GSMTC is unreliable for Spotify
    global _peak_meter
    try:
        if _peak_meter is None:
            speakers = AudioUtilities.GetSpeakers()
            _peak_meter = cast(
                speakers._dev.Activate(IAudioMeterInformation._iid_, CLSCTX_ALL, None),
                POINTER(IAudioMeterInformation)
            )
        return round(_peak_meter.GetPeakValue(), 4)
    except Exception:
        _peak_meter = None
        return 0.0
        return 0.0

_mgr = None

async def get_mgr():
    global _mgr
    if _mgr is None:
        _mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    return _mgr

async def get_session():
    try:
        mgr = await get_mgr()
        return mgr.get_current_session() if mgr else None
    except Exception:
        global _mgr
        _mgr = None
        return None

async def poll():
    try:
        import time
        session = await get_session()
        if not session:
            return {}
        mp  = await session.try_get_media_properties_async()
        tl  = session.get_timeline_properties()
        pb  = session.get_playback_info()

        sampled_at_ms = int(time.time() * 1000)

        pos_raw = int(tl.position.total_seconds() * 1000) if tl else 0
        dur = int((tl.end_time.total_seconds() - tl.start_time.total_seconds()) * 1000) if tl else 0

        # Windows updates tl.position lazily — correct using last_updated_time
        pos = pos_raw
        if tl and pb and int(pb.playback_status) == 3:
            try:
                updated_ticks = tl.last_updated_time
                EPOCH_DIFF = 116444736000000000  # ticks from 1601 to 1970
                updated_ms = (updated_ticks - EPOCH_DIFF) // 10_000
                elapsed = sampled_at_ms - updated_ms
                if 0 < elapsed < 30_000:
                    pos = pos_raw + elapsed
            except Exception:
                pass

        state_int = int(pb.playback_status) if pb else 0
        app_lower = (session.source_app_user_model_id or "").lower()
        is_browser = any(x in app_lower for x in ('chrome', 'firefox', 'msedge', 'opera', 'brave'))
        # Browsers report state=5 (Stopped) even when playing
        actually_playing = (state_int == 3) or (is_browser and state_int not in (4,))

        return {
            "title":      mp.title  if mp else "",
            "artist":     mp.artist if mp else "",
            "appId":      session.source_app_user_model_id or "",
            "positionMs": pos,
            "durationMs": dur,
            "state":      state_int,
            "sampledAt":  sampled_at_ms,
            "peak":       get_peak(session.source_app_user_model_id or "", actually_playing),
        }
    except Exception as e:
        global _mgr
        _mgr = None
        return {"error": str(e)}

async def get_thumb():
    """Return base64-encoded PNG of current track thumbnail via iTunes Search API."""
    try:
        session = await get_session()
        if not session:
            return ""
        mp = await session.try_get_media_properties_async()
        if not mp:
            return ""
        title  = mp.title  or ""
        artist = mp.artist or ""
        if not title:
            return ""
        import urllib.request, urllib.parse, json as _json

        def search_art(artist, title):
            # Try Deezer first (better for non-English)
            try:
                q = urllib.parse.quote(f"{artist} {title}")
                url = f"https://api.deezer.com/search?q={q}&limit=1"
                req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
                with urllib.request.urlopen(req, timeout=5) as resp:
                    data = _json.loads(resp.read())
                items = data.get("data", [])
                if items:
                    return items[0].get("album", {}).get("cover_xl") or items[0].get("album", {}).get("cover_big", "")
            except Exception:
                pass
            # Fallback: iTunes
            try:
                for query in [f"{artist} {title}", artist, title]:
                    if not query.strip():
                        continue
                    q = urllib.parse.quote(query)
                    url = f"https://itunes.apple.com/search?term={q}&media=music&limit=1&entity=song"
                    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
                    with urllib.request.urlopen(req, timeout=5) as resp:
                        data = _json.loads(resp.read())
                    results = data.get("results", [])
                    if results:
                        art = results[0].get("artworkUrl100", "").replace("100x100bb", "600x600bb")
                        if art:
                            return art
            except Exception:
                pass
            return ""

        art_url = search_art(artist, title)
        if not art_url:
            return ""        # Get higher resolution (600x600)
        art_url = art_url.replace("100x100bb", "600x600bb")
        with urllib.request.urlopen(art_url, timeout=5) as resp:
            img_bytes = resp.read()
        # Convert to PNG and resize to exact THUMB size
        try:
            from PIL import Image as _Image
            import io as _io
            THUMB = 64  # must be power of 2 for GPU texture
            img = _Image.open(_io.BytesIO(img_bytes)).convert("RGB")  # RGB, no alpha issues
            img = img.resize((THUMB, THUMB), _Image.LANCZOS)
            # Convert to RGBA with full alpha
            img_rgba = _Image.new("RGBA", img.size, (0, 0, 0, 255))
            img_rgba.paste(img)
            buf = _io.BytesIO()
            img_rgba.save(buf, format="PNG")
            img_bytes = buf.getvalue()
            buf = _io.BytesIO()
            img.save(buf, format="PNG")
            img_bytes = buf.getvalue()
        except Exception:
            pass
        return base64.b64encode(img_bytes).decode('ascii')
    except Exception:
        return ""

def _read_thumb_sync(thumbnail_ref):
    """Read thumbnail via PowerShell to avoid winsdk stream hang."""
    try:
        ps_script = r"""
Add-Type -AssemblyName System.Runtime.WindowsRuntime
$op = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media,ContentType=WindowsRuntime]::RequestAsync()
$deadline = [DateTime]::UtcNow.AddSeconds(5)
while ([int]$op.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
if ([int]$op.Status -ne 1) { exit 1 }
$mgr = $op.GetResults()
$session = $mgr.GetCurrentSession()
if ($null -eq $session) { exit 1 }
$mpOp = $session.TryGetMediaPropertiesAsync()
$deadline = [DateTime]::UtcNow.AddSeconds(3)
while ([int]$mpOp.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
if ([int]$mpOp.Status -ne 1) { exit 1 }
$mp = $mpOp.GetResults()
if ($null -eq $mp -or $null -eq $mp.Thumbnail) { exit 1 }
$streamOp = $mp.Thumbnail.OpenReadAsync()
$deadline = [DateTime]::UtcNow.AddSeconds(3)
while ([int]$streamOp.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
if ([int]$streamOp.Status -ne 1) { exit 1 }
$stream = $streamOp.GetResults()
$size = $stream.Size
if ($size -eq 0) { exit 1 }
$reader = [Windows.Storage.Streams.DataReader,Windows.Storage,ContentType=WindowsRuntime]::new($stream)
$loadOp = $reader.LoadAsync($size)
$deadline = [DateTime]::UtcNow.AddSeconds(3)
while ([int]$loadOp.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
$buf = New-Object byte[] $size
$reader.ReadBytes($buf)
[Convert]::ToBase64String($buf)
"""
        result = subprocess.run(
            ["powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", ps_script],
            capture_output=True, text=True, timeout=15
        )
        b64 = result.stdout.strip()
        if b64 and len(b64) > 100:
            return b64
        return ""
    except Exception:
        return ""

async def skip(direction):
    try:
        session = await get_session()
        if not session:
            return
        if direction == "NEXT":
            await session.try_skip_next_async()
        else:
            await session.try_skip_previous_async()
    except Exception:
        pass

async def toggle_play_pause():
    try:
        session = await get_session()
        if session:
            await session.try_toggle_play_pause_async()
    except Exception:
        pass

async def seek(position_ms):
    try:
        session = await get_session()
        if session:
            ticks = int(position_ms) * 10_000  # ms -> 100ns ticks
            await session.try_change_playback_position_async(ticks)
    except Exception:
        pass

def main():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    for line in sys.stdin:
        cmd = line.strip()
        if cmd == "POLL":
            result = loop.run_until_complete(poll())
            print(json.dumps(result, ensure_ascii=False))
        elif cmd == "THUMB":
            b64 = loop.run_until_complete(get_thumb())
            print(b64 if b64 else "NONE")
        elif cmd == "SKIP_NEXT":
            loop.run_until_complete(skip("NEXT"))
            print("OK")
        elif cmd == "SKIP_PREV":
            loop.run_until_complete(skip("PREV"))
            print("OK")
        elif cmd == "PLAY_PAUSE":
            loop.run_until_complete(toggle_play_pause())
            print("OK")
        elif cmd.startswith("SEEK "):
            try:
                ms = int(cmd[5:])
                loop.run_until_complete(seek(ms))
            except Exception:
                pass
            print("OK")
        else:
            print("{}")

if __name__ == "__main__":
    main()
