import asyncio, base64
import winsdk.windows.media.control as wmc
import winsdk.windows.storage.streams as wss

async def main():
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    mp = await session.try_get_media_properties_async()
    stream = await mp.thumbnail.open_read_async()
    reader = wss.DataReader(stream)
    loaded = await reader.load_async(stream.size)
    buf = bytes(reader.read_buffer(loaded))
    # Save first few bytes to identify format
    print("First 8 bytes (hex):", buf[:8].hex())
    print("PNG magic:", buf[:4] == b'\x89PNG')
    print("JPEG magic:", buf[:2] == b'\xff\xd8')
    # Save to file to inspect
    with open("tools/thumb_test.bin", "wb") as f:
        f.write(buf)
    print("Saved to tools/thumb_test.bin, size:", len(buf))

asyncio.run(main())
