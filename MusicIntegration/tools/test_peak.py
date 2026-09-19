from ctypes import cast, POINTER
from comtypes import CLSCTX_ALL
from pycaw.pycaw import AudioUtilities, IAudioMeterInformation

speakers = AudioUtilities.GetSpeakers()
# AudioDevice.EndpointVolume gives us the IMMDevice-backed interface
# We need the raw IMMDevice — it's in speakers._dev
print("_dev type:", type(speakers._dev))
meter = cast(speakers._dev.Activate(IAudioMeterInformation._iid_, CLSCTX_ALL, None),
             POINTER(IAudioMeterInformation))
print("Master peak:", meter.GetPeakValue())
