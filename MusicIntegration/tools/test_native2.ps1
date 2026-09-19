Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;

public static class WinRtTest2 {
    [DllImport("combase.dll")]
    static extern int RoInitialize(int initType);

    [DllImport("combase.dll", CharSet = CharSet.Unicode)]
    static extern int WindowsCreateString(string src, int length, out IntPtr hstring);

    [DllImport("combase.dll")]
    static extern int WindowsDeleteString(IntPtr hstring);

    [DllImport("combase.dll")]
    static extern int RoGetActivationFactory(IntPtr hstring, ref Guid riid, out IntPtr factory);

    static readonly Guid IActivationFactory = new Guid("00000035-0000-0000-C000-000000000046");

    public static string Test() {
        int hr = RoInitialize(1);
        if (hr < 0 && hr != unchecked((int)0x80010106))
            return "RoInitialize failed: 0x" + hr.ToString("X8");

        string cls = "Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager";
        IntPtr hstr;
        hr = WindowsCreateString(cls, cls.Length, out hstr);
        if (hr < 0) return "WindowsCreateString failed: 0x" + hr.ToString("X8");

        Guid iid = IActivationFactory;
        IntPtr factory;
        hr = RoGetActivationFactory(hstr, ref iid, out factory);
        WindowsDeleteString(hstr);

        if (hr < 0) return "RoGetActivationFactory failed: 0x" + hr.ToString("X8");

        // Try calling RequestAsync (vtable slot 6)
        // vtable[0]=QI, [1]=AddRef, [2]=Release, [3]=GetIids, [4]=GetRuntimeClassName, [5]=GetTrustLevel, [6]=ActivateInstance
        // But we need IGSMTCSessionManagerStatics::RequestAsync, not IActivationFactory::ActivateInstance
        // Let's QI for the statics interface
        return "Factory OK: 0x" + factory.ToString("X");
    }
}
'@ -Language CSharp

Write-Host ([WinRtTest2]::Test())
