"""Try to get Spotify album art URL directly"""
import asyncio
import winsdk.windows.media.control as wmc

async def main():
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    mp = await session.try_get_media_properties_async()
    print("title:", mp.title)
    thumb = mp.thumbnail
    print("thumb type:", type(thumb))
    print("thumb dir:", [x for x in dir(thumb) if not x.startswith('_')])
    # Try to get as string/uri
    try:
        print("str:", str(thumb))
    except: pass

asyncio.run(main())
