Add-Type -AssemblyName System.Runtime.WindowsRuntime

$op = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media,ContentType=WindowsRuntime]::RequestAsync()
$deadline = [DateTime]::UtcNow.AddSeconds(5)
while ([int]$op.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
Write-Host "mgr status: $([int]$op.Status)"
if ([int]$op.Status -ne 1) { Write-Host "mgr failed"; exit 1 }
$mgr = $op.GetResults()
$session = $mgr.GetCurrentSession()
Write-Host "session: $session"
if ($null -eq $session) { Write-Host "no session"; exit 1 }

$mpOp = $session.TryGetMediaPropertiesAsync()
$deadline = [DateTime]::UtcNow.AddSeconds(3)
while ([int]$mpOp.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
Write-Host "mp status: $([int]$mpOp.Status)"
if ([int]$mpOp.Status -ne 1) { Write-Host "mp failed"; exit 1 }
$mp = $mpOp.GetResults()
Write-Host "title: $($mp.Title)"
Write-Host "thumbnail null: $($null -eq $mp.Thumbnail)"

if ($null -ne $mp.Thumbnail) {
    $streamOp = $mp.Thumbnail.OpenReadAsync()
    $deadline = [DateTime]::UtcNow.AddSeconds(3)
    while ([int]$streamOp.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) { Start-Sleep -Milliseconds 20 }
    Write-Host "stream status: $([int]$streamOp.Status)"
    if ([int]$streamOp.Status -eq 1) {
        $stream = $streamOp.GetResults()
        Write-Host "stream size: $($stream.Size)"
    }
}
