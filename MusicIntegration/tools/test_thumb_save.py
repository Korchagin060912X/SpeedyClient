import asyncio, base64, io
import winsdk.windows.media.control as wmc
import urllib.request, urllib.parse, json

async def main():
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    mp = await session.try_get_media_properties_async()
    print(f"title: {mp.title}, artist: {mp.artist}")
    
    from PIL import Image
    query = urllib.parse.quote(f"{mp.artist} {mp.title}")
    url = f"https://itunes.apple.com/search?term={query}&media=music&limit=1&entity=song"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=5) as resp:
        data = json.loads(resp.read())
    results = data.get("results", [])
    if not results:
        print("not found on iTunes")
        return
    art_url = results[0]["artworkUrl100"].replace("100x100bb", "600x600bb")
    print(f"art_url: {art_url}")
    with urllib.request.urlopen(art_url, timeout=5) as resp:
        img_bytes = resp.read()
    
    img = Image.open(io.BytesIO(img_bytes)).convert("RGB").resize((64, 64))
    img_rgba = Image.new("RGBA", img.size, (0, 0, 0, 255))
    img_rgba.paste(img)
    img_rgba.save("tools/thumb_final.png")
    print(f"saved, size={img_rgba.size}, mode={img_rgba.mode}")
    px = img_rgba.getpixel((5, 5))
    print(f"pixel(5,5) RGBA: {px}")

asyncio.run(main())
