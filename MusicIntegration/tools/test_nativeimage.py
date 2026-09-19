# Check what the thumbnail looks like - save as actual image file
import asyncio, base64, io
import winsdk.windows.media.control as wmc
import winsdk.windows.storage.streams as wss
from PIL import Image

async def main():
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    mp = await session.try_get_media_properties_async()
    stream = await mp.thumbnail.open_read_async()
    reader = wss.DataReader(stream)
    await reader.load_async(stream.size)
    buf = bytes(reader.read_buffer(stream.size))
    
    img = Image.open(io.BytesIO(buf))
    print(f"Mode: {img.mode}, Size: {img.size}")
    img_rgba = img.convert("RGBA")
    px = img_rgba.getpixel((5, 5))
    print(f"Pixel (5,5) RGBA: {px}")
    img.save("tools/thumb_preview.png")
    print("Saved to tools/thumb_preview.png")

asyncio.run(main())
