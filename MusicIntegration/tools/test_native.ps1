# Quick sanity check: can we call RoGetActivationFactory from PowerShell via P/Invoke?
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;

public static class WinRtTest {
    [DllImport("combase.dll")]
    static extern int RoInitialize(int initType);

    [DllImport("combase.dll", CharSet = CharSet.Unicode)]
    static extern int WindowsCreateString(string src, int length, out IntPtr hstring);

    [DllImport("combase.dll")]
    static extern int WindowsDeleteString(IntPtr hstring);

    [DllImport("combase.dll")]
    static extern int RoGetActivationFactory(IntPtr hstring, ref Guid riid, out IntPtr factory);

    // IActivationFactory IID
    static readonly Guid IActivationFactory = new Guid("00000035-0000-0000-C000-000000000046");

    public static string Test() {
        int hr = RoInitialize(1); // MTA
        if (hr < 0 && hr != unchecked((int)0x80010106)) // already init
            return "RoInitialize failed: 0x" + hr.ToString("X8");

        IntPtr hstr;
        hr = WindowsCreateString("Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager", 71, out hstr);
        if (hr < 0) return "WindowsCreateString failed: 0x" + hr.ToString("X8");

        Guid iid = IActivationFactory;
        IntPtr factory;
        hr = RoGetActivationFactory(hstr, ref iid, out factory);
        WindowsDeleteString(hstr);

        if (hr < 0) return "RoGetActivationFactory failed: 0x" + hr.ToString("X8");
        return "Factory OK: 0x" + factory.ToString("X");
    }
}
'@ -Language CSharp

Write-Host ([WinRtTest]::Test())
