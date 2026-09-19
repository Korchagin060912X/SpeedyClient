Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using System.Threading;

public static class VtableTest {
    [DllImport("combase.dll")] static extern int RoInitialize(int t);
    [DllImport("combase.dll", CharSet=CharSet.Unicode)] static extern int WindowsCreateString(string s, int len, out IntPtr h);
    [DllImport("combase.dll")] static extern int WindowsDeleteString(IntPtr h);
    [DllImport("combase.dll")] static extern int RoGetActivationFactory(IntPtr h, ref Guid iid, out IntPtr f);
    [DllImport("combase.dll", CharSet=CharSet.Unicode)] static extern IntPtr WindowsGetStringRawBuffer(IntPtr h, out int len);

    static readonly Guid IActivationFactory = new Guid("00000035-0000-0000-C000-000000000046");
    static readonly Guid IGSMTCManagerStatics = new Guid("2050c4ee-11a0-57de-aed7-c97c70338245");

    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int QueryInterfaceD(IntPtr t, ref Guid iid, out IntPtr r);

    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int RequestAsyncD(IntPtr t, out IntPtr r);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int GetCurrentSessionD(IntPtr t, out IntPtr r);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int GetStatusD(IntPtr t, out int s);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int GetResultsD(IntPtr t, out IntPtr r);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int GetHStringD(IntPtr t, out IntPtr r);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int GetIntD(IntPtr t, out int r);
    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int GetAsyncD(IntPtr t, out IntPtr r);

    static IntPtr Vtbl(IntPtr obj, int slot) {
        IntPtr vtbl = Marshal.ReadIntPtr(obj);
        return Marshal.ReadIntPtr(vtbl, slot * IntPtr.Size);
    }

    static IntPtr WaitAsync(IntPtr asyncOp, int ms) {
        if (asyncOp == IntPtr.Zero) return IntPtr.Zero;
        var getStatus = (GetStatusD)Marshal.GetDelegateForFunctionPointer(Vtbl(asyncOp, 7), typeof(GetStatusD));
        var deadline = DateTime.UtcNow.AddMilliseconds(ms);
        while (DateTime.UtcNow < deadline) {
            int status; getStatus(asyncOp, out status);
            if (status == 1) {
                var getResults = (GetResultsD)Marshal.GetDelegateForFunctionPointer(Vtbl(asyncOp, 11), typeof(GetResultsD));
                IntPtr result; int hr = getResults(asyncOp, out result);
                return hr >= 0 ? result : IntPtr.Zero;
            }
            if (status != 0) return IntPtr.Zero;
            Thread.Sleep(10);
        }
        return IntPtr.Zero;
    }

    static string ReadHStr(IntPtr h) {
        if (h == IntPtr.Zero) return "";
        int len; IntPtr buf = WindowsGetStringRawBuffer(h, out len);
        string s = buf != IntPtr.Zero ? Marshal.PtrToStringUni(buf, len) : "";
        WindowsDeleteString(h);
        return s;
    }

    public static string Test() {
        RoInitialize(1);
        string cls = "Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager";
        IntPtr hstr; WindowsCreateString(cls, cls.Length, out hstr);
        Guid iid = IActivationFactory;
        IntPtr factory; int hr0 = RoGetActivationFactory(hstr, ref iid, out factory); WindowsDeleteString(hstr);
        if (hr0 < 0) return "RoGetActivationFactory failed: 0x" + hr0.ToString("X8");

        // QI for IGSMTCSessionManagerStatics
        var qi = (QueryInterfaceD)Marshal.GetDelegateForFunctionPointer(Vtbl(factory, 0), typeof(QueryInterfaceD));
        Guid staticsIid = IGSMTCManagerStatics;
        IntPtr statics; int hrQi = qi(factory, ref staticsIid, out statics);
        if (hrQi < 0) return "QI for statics failed: 0x" + hrQi.ToString("X8");

        // Slot 6 = RequestAsync on IGSMTCSessionManagerStatics
        var requestAsync = (RequestAsyncD)Marshal.GetDelegateForFunctionPointer(Vtbl(statics, 6), typeof(RequestAsyncD));
        IntPtr asyncOp; int hr1 = requestAsync(statics, out asyncOp);
        if (hr1 < 0) return "RequestAsync failed: 0x" + hr1.ToString("X8");

        IntPtr mgr = WaitAsync(asyncOp, 5000);
        if (mgr == IntPtr.Zero) return "WaitAsync mgr timed out";

        var getCurrentSession = (GetCurrentSessionD)Marshal.GetDelegateForFunctionPointer(Vtbl(mgr, 6), typeof(GetCurrentSessionD));
        IntPtr session; int hr2 = getCurrentSession(mgr, out session);
        if (hr2 < 0) return "GetCurrentSession failed: 0x" + hr2.ToString("X8");
        if (session == IntPtr.Zero) return "No active session (nothing playing?)";

        // slot 6 = get_SourceAppUserModelId
        var getAppId = (GetHStringD)Marshal.GetDelegateForFunctionPointer(Vtbl(session, 6), typeof(GetHStringD));
        IntPtr appIdH; getAppId(session, out appIdH);
        string appId = ReadHStr(appIdH);

        // slot 7 = TryGetMediaPropertiesAsync
        var getMpAsync = (GetAsyncD)Marshal.GetDelegateForFunctionPointer(Vtbl(session, 7), typeof(GetAsyncD));
        IntPtr mpAsync; getMpAsync(session, out mpAsync);
        IntPtr mp = WaitAsync(mpAsync, 3000);

        string title = "", artist = "";
        if (mp != IntPtr.Zero) {
            var getTitle = (GetHStringD)Marshal.GetDelegateForFunctionPointer(Vtbl(mp, 6), typeof(GetHStringD));
            IntPtr titleH; getTitle(mp, out titleH); title = ReadHStr(titleH);

            var getArtist = (GetHStringD)Marshal.GetDelegateForFunctionPointer(Vtbl(mp, 8), typeof(GetHStringD));
            IntPtr artistH; getArtist(mp, out artistH); artist = ReadHStr(artistH);
        }

        // slot 9 = GetPlaybackInfoAsync
        var getPbAsync = (GetAsyncD)Marshal.GetDelegateForFunctionPointer(Vtbl(session, 9), typeof(GetAsyncD));
        IntPtr pbAsync; getPbAsync(session, out pbAsync);
        IntPtr pb = WaitAsync(pbAsync, 3000);
        int playbackStatus = 0;
        if (pb != IntPtr.Zero) {
            var getPlaybackStatus = (GetIntD)Marshal.GetDelegateForFunctionPointer(Vtbl(pb, 10), typeof(GetIntD));
            getPlaybackStatus(pb, out playbackStatus);
        }

        return "appId=" + appId + " | title=" + title + " | artist=" + artist + " | state=" + playbackStatus;
    }
}
'@ -Language CSharp

Write-Host ([VtableTest]::Test())
