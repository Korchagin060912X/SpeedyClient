Add-Type -AssemblyName System.Runtime.WindowsRuntime
try {
    $op = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media,ContentType=WindowsRuntime]::RequestAsync()
    Write-Host "op type: $($op.GetType().FullName)"
    Start-Sleep -Milliseconds 2000
    Write-Host "status: $($op.Status)"
    if ($op.Status -eq [Windows.Foundation.AsyncStatus]::Completed) {
        $mgr = $op.GetResults()
        Write-Host "mgr: $mgr"
        $session = $mgr.GetCurrentSession()
        Write-Host "session: $session"
        if ($null -ne $session) {
            Write-Host "appId: $($session.SourceAppUserModelId)"
        }
    } else {
        Write-Host "op not completed, status: $($op.Status)"
        Write-Host "error: $($op.ErrorCode)"
    }
} catch {
    Write-Host "EXCEPTION: $($_.Exception.ToString())"
}
