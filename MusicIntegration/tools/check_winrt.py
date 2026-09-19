try:
    import winrt.windows.media.control as wmc
    print("winrt ok")
except ImportError as e:
    print("no winrt:", e)
