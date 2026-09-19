Add-Type -AssemblyName System.Windows.Forms

Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Forms;

public static class StaTest {
    [DllImport("combase.dll")] static extern int RoInitialize(int t);
    [DllImport("combase.dll", CharSet=CharSet.Unicode)] static extern int WindowsCreateString(string s, int len, out IntPtr h);
    [DllImport("combase.dll")] static extern int WindowsDeleteString(IntPtr h);
    [DllImport("combase.dll")] static extern int RoGetActivationFactory(IntPtr h, ref Guid iid, out IntPtr f);
    [DllImport("combase.dll", CharSet=CharSet.Unicode)] static extern IntPtr WindowsGetStringRawBuffer(IntPtr h, out int len);

    static readonly Guid IActivationFactory  = new Guid("00000035-0000-0000-C000-000000000046");
    static readonly Guid IGSMTCManagerStatics = new Guid("2050c4ee-11a0-57de-aed7-c97c70338245");

    [UnmanagedFunctionPointer(CallingConvention.StdCall)] delegate int QID(IntPtr t, ref Guid iid, out IntPtr r);
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

    static string ReadHStr(IntPtr h) {
        if (h == IntPtr.Zero) return "";
        int len; IntPtr buf = WindowsGetStringRawBuffer(h, out len);
        string s = buf != IntPtr.Zero ? Marshal.PtrToStringUni(buf, len) : "";
        WindowsDeleteString(h);
        return s;
    }

    static string _result;

    public static string Run() {
        var thread = new Thread(() => {
            try {
                // RO_INIT_STA = 0
                RoInitialize(0);

                string cls = "Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager";
                IntPtr hstr; WindowsCreateString(cls, cls.Length, out hstr);
                Guid iid = IActivationFactory;
                IntPtr factory; RoGetActivationFactory(hstr, ref iid, out factory); WindowsDeleteString(hstr);

                var qi = (QID)Marshal.GetDelegateForFunctionPointer(Vtbl(factory, 0), typeof(QID));
                Guid staticsIid = IGSMTCManagerStatics;
                IntPtr statics; qi(factory, ref staticsIid, out statics);

                var requestAsync = (RequestAsyncD)Marshal.GetDelegateForFunctionPointer(Vtbl(statics, 6), typeof(RequestAsyncD));
                IntPtr asyncOp; requestAsync(statics, out asyncOp);

                // Poll status while pumping messages
                var getStatus = (GetStatusD)Marshal.GetDelegateForFunctionPointer(Vtbl(asyncOp, 7), typeof(GetStatusD));
                var deadline = DateTime.UtcNow.AddSeconds(8);
                int status = 0;
                while (DateTime.UtcNow < deadline) {
                    Application.DoEvents();
                    getStatus(asyncOp, out status);
                    if (status != 0) break;
                    Thread.Sleep(10);
                }

                if (status != 1) { _result = "async not completed, status=" + status; return; }

                var getResults = (GetResultsD)Marshal.GetDelegateForFunctionPointer(Vtbl(asyncOp, 11), typeof(GetResultsD));
                IntPtr mgr; getResults(asyncOp, out mgr);

                var getCurrentSession = (GetCurrentSessionD)Marshal.GetDelegateForFunctionPointer(Vtbl(mgr, 6), typeof(GetCurrentSessionD));
                IntPtr session; getCurrentSession(mgr, out session);
                if (session == IntPtr.Zero) { _result = "no session"; return; }

                var getAppId = (GetHStringD)Marshal.GetDelegateForFunctionPointer(Vtbl(session, 6), typeof(GetHStringD));
                IntPtr appIdH; getAppId(session, out appIdH);
                string appId = ReadHStr(appIdH);

                var getMpAsync = (GetAsyncD)Marshal.GetDelegateForFunctionPointer(Vtbl(session, 7), typeof(GetAsyncD));
                IntPtr mpAsync; getMpAsync(session, out mpAsync);

                // Wait for media props
                var getMpStatus = (GetStatusD)Marshal.GetDelegateForFunctionPointer(Vtbl(mpAsync, 7), typeof(GetStatusD));
                deadline = DateTime.UtcNow.AddSeconds(3);
                while (DateTime.UtcNow < deadline) {
                    Application.DoEvents();
                    getMpStatus(mpAsync, out status);
                    if (status != 0) break;
                    Thread.Sleep(10);
                }
                IntPtr mp = IntPtr.Zero;
                if (status == 1) {
                    var getMpResults = (GetResultsD)Marshal.GetDelegateForFunctionPointer(Vtbl(mpAsync, 11), typeof(GetResultsD));
                    getMpResults(mpAsync, out mp);
                }

                string title = "", artist = "";
                if (mp != IntPtr.Zero) {
                    var getTitle = (GetHStringD)Marshal.GetDelegateForFunctionPointer(Vtbl(mp, 6), typeof(GetHStringD));
                    IntPtr titleH; getTitle(mp, out titleH); title = ReadHStr(titleH);
                    var getArtist = (GetHStringD)Marshal.GetDelegateForFunctionPointer(Vtbl(mp, 8), typeof(GetHStringD));
                    IntPtr artistH; getArtist(mp, out artistH); artist = ReadHStr(artistH);
                }

                _result = "appId=" + appId + " | title=" + title + " | artist=" + artist;
            } catch (Exception ex) {
                _result = "EXCEPTION: " + ex.Message;
            }
        });
        thread.SetApartmentState(ApartmentState.STA);
        thread.Start();
        thread.Join(15000);
        return _result ?? "null result";
    }
}
'@ -Language CSharp -ReferencedAssemblies "System.Windows.Forms"

Write-Host ([StaTest]::Run())
