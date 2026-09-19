# Test: use Add-Type C# with async Task to call WinRT GSMTC
Add-Type -AssemblyName System.Runtime.WindowsRuntime

$code = @'
using System;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.Media.Control;

public static class GsmtcReader {
    public static string Poll() {
        try {
            var task = Task.Run(async () => {
                var mgr = await GlobalSystemMediaTransportControlsSessionManager.RequestAsync();
                if (mgr == null) return "{}";
                var session = mgr.GetCurrentSession();
                if (session == null) return "{}";
                var mp    = await session.TryGetMediaPropertiesAsync();
                var tl    = await session.GetTimelinePropertiesAsync();
                var pb    = await session.GetPlaybackInfoAsync();
                string title  = mp?.Title  ?? "";
                string artist = mp?.Artist ?? "";
                string appId  = session.SourceAppUserModelId ?? "";
                long   pos    = (long)(tl?.Position.TotalMilliseconds ?? 0);
                long   dur    = tl != null ? (long)(tl.EndTime.TotalMilliseconds - tl.StartTime.TotalMilliseconds) : 0;
                int    state  = (int)(pb?.PlaybackStatus ?? 0);
                title  = title .Replace("\\","\\\\").Replace("\"","\\\"");
                artist = artist.Replace("\\","\\\\").Replace("\"","\\\"");
                appId  = appId .Replace("\\","\\\\").Replace("\"","\\\"");
                return $"{{\"title\":\"{title}\",\"artist\":\"{artist}\",\"appId\":\"{appId}\",\"positionMs\":{pos},\"durationMs\":{dur},\"state\":{state}}}";
            });
            return task.GetAwaiter().GetResult();
        } catch (Exception ex) {
            return "{\"error\":\"" + ex.Message.Replace("\"","'") + "\"}";
        }
    }
}
'@

$refs = @(
    [System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeMarshal].Assembly.Location,
    (Get-ChildItem "C:\Windows\System32\WinMetadata\Windows.Media.winmd" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)
)
$refs = $refs | Where-Object { $_ -ne $null -and (Test-Path $_) }

Write-Host "Refs: $refs"

try {
    Add-Type -TypeDefinition $code -ReferencedAssemblies $refs -Language CSharp
    Write-Host "Compiled OK"
    $result = [GsmtcReader]::Poll()
    Write-Host "Result: $result"
} catch {
    Write-Host "Compile/Run error: $($_.Exception.ToString())"
}
