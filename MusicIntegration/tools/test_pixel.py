from PIL import Image
import io, base64
import winsdk.windows.media.control as wmc
import winsdk.windows.storage.streams as wss
import asyncio

async def main():
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    mp = await session.try_get_media_properties_async()
    stream = await mp.thumbnail.open_read_async()
    reader = wss.DataReader(stream)
    await reader.load_async(stream.size)
    buf = bytes(reader.read_buffer(stream.size))
    img = Image.open(io.BytesIO(buf)).convert("RGBA")
    # Print top-left pixel
    r, g, b, a = img.getpixel((0, 0))
    print(f"Top-left pixel RGBA: R={r} G={g} B={b} A={a}")
    print(f"As hex ARGB: 0x{a:02X}{r:02X}{g:02X}{b:02X}")
    print(f"As hex ABGR: 0x{a:02X}{b:02X}{g:02X}{r:02X}")

asyncio.run(main())
