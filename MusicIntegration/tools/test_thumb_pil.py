"""Test reading Spotify thumbnail via Windows shell instead of winsdk streams"""
import asyncio, base64, subprocess, os, tempfile
import winsdk.windows.media.control as wmc

async def get_thumb_via_ps():
    """Use PowerShell to extract thumbnail — avoids winsdk stream hang"""
    mgr = await wmc.GlobalSystemMediaTransportControlsSessionManager.request_async()
    session = mgr.get_current_session()
    if not session:
        return ""
    mp = await session.try_get_media_properties_async()
    if not mp or not mp.thumbnail:
        return ""
    
    # Get the thumbnail URI string if available
    try:
        # Try to get thumbnail as IRandomAccessStreamReference and save via PS
        thumb_ref = mp.thumbnail
        print(f"thumb_ref type: {type(thumb_ref)}")
        print(f"thumb_ref dir: {[x for x in dir(thumb_ref) if not x.startswith('_')]}")
    except Exception as e:
        print(f"Error: {e}")
    return ""

asyncio.run(get_thumb_via_ps())
