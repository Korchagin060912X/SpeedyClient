Add-Type -AssemblyName System.Runtime.WindowsRuntime

# Check actual numeric status
$op = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media,ContentType=WindowsRuntime]::RequestAsync()
Write-Host "op: $op"
Write-Host "op type: $($op.GetType().FullName)"

# Status as int
$statusInt = [int]$op.Status
Write-Host "status int: $statusInt"  # 0=Started, 1=Completed, 2=Canceled, 3=Error

Start-Sleep -Milliseconds 3000
$statusInt = [int]$op.Status
Write-Host "status int after 3s: $statusInt"

if ($statusInt -eq 1) {
    $mgr = $op.GetResults()
    Write-Host "mgr: $mgr"
    $session = $mgr.GetCurrentSession()
    Write-Host "session: $session"
} else {
    Write-Host "Still not completed. ErrorCode: $($op.ErrorCode)"
}
