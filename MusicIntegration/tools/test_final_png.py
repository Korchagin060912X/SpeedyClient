import asyncio, base64, io, sys
sys.stdin = open('nul')

import winsdk.windows.media.control as wmc
import urllib.request, urllib.parse, json
from PIL import Image

async def main():
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    mp = await session.try_get_media_properties_async()
    artist, title = mp.artist, mp.title
    print(f"{artist} - {title}")

    q = urllib.parse.quote(f"{artist} {title}")
    url = f"https://api.deezer.com/search?q={q}&limit=1"
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent":"Mozilla/5.0"}), timeout=5) as r:
        items = json.loads(r.read()).get("data", [])
    if not items:
        print("not found"); return
    
    cover_url = items[0]["album"].get("cover_xl") or items[0]["album"].get("cover_big","")
    with urllib.request.urlopen(cover_url, timeout=5) as r:
        img_bytes = r.read()

    img = Image.open(io.BytesIO(img_bytes)).convert("RGB").resize((64,64))
    img_rgba = Image.new("RGBA", (64,64), (0,0,0,255))
    img_rgba.paste(img)
    img_rgba.save("tools/thumb_final.png")
    px = img_rgba.getpixel((5,5))
    print(f"pixel(5,5) RGBA={px}")
    print(f"saved 64x64 PNG, size={len(open('tools/thumb_final.png','rb').read())} bytes")

asyncio.run(main())
