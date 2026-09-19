$ErrorActionPreference = 'SilentlyContinue'
[Console]::InputEncoding  = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)

Add-Type -AssemblyName System.Runtime.WindowsRuntime

# Find AsTask<T>(IAsyncOperation<T>) once
$asTaskMethod = ([System.WindowsRuntimeSystemExtensions].GetMethods() |
    Where-Object { $_.Name -eq 'AsTask' -and $_.IsGenericMethod -and $_.GetParameters().Count -eq 1 } |
    Select-Object -First 1)

# Shared state box passed into STA threads via closure
$box = [hashtable]::Synchronized(@{ result = $null; error = $null })

function Run-OnSta([scriptblock]$sb, [hashtable]$shared, [int]$timeoutMs = 8000) {
    $t = [System.Threading.Thread]::new([System.Threading.ParameterizedThreadStart]{
        param($state)
        try {
            & $state.sb $state.shared
        } catch {
            $state.shared['error'] = $_.Exception.Message
        }
    })
    $t.SetApartmentState([System.Threading.ApartmentState]::STA)
    $t.IsBackground = $true
    $t.Start(@{ sb = $sb; shared = $shared })
    $null = $t.Join($timeoutMs)
}

function Await-Op($async) {
    if ($null -eq $async) { return $null }
    $iface = $async.GetType().GetInterface('Windows.Foundation.IAsyncOperation`1')
    if ($null -ne $iface -and $null -ne $asTaskMethod) {
        $T      = $iface.GetGenericArguments()[0]
        $method = $asTaskMethod.MakeGenericMethod($T)
        $task   = $method.Invoke($null, @($async))
        $null   = $task.Wait(5000)
        return $task.Result
    }
    $deadline = [DateTime]::UtcNow.AddSeconds(5)
    while ([int]$async.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) {
        [System.Threading.Thread]::Sleep(10)
    }
    if ([int]$async.Status -eq 1) { return $async.GetResults() }
    return $null
}

# Init manager on STA thread
$initBox = [hashtable]::Synchronized(@{ mgr = $null; error = $null })
Run-OnSta -timeoutMs 10000 -shared $initBox -sb {
    param($s)
    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $op  = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media,ContentType=WindowsRuntime]::RequestAsync()
    # spin-wait since AsTask may not be available yet at this point
    $deadline = [DateTime]::UtcNow.AddSeconds(8)
    while ([int]$op.Status -eq 0 -and [DateTime]::UtcNow -lt $deadline) {
        [System.Threading.Thread]::Sleep(20)
    }
    if ([int]$op.Status -eq 1) {
        $s['mgr'] = $op.GetResults()
    } else {
        $s['error'] = "RequestAsync status=$([int]$op.Status)"
    }
}

if ($null -eq $initBox['mgr']) {
    $errMsg = $initBox['error']
    if ($null -eq $errMsg) { $errMsg = 'timeout' }
    while ($true) {
        $line = [Console]::In.ReadLine()
        if ($null -eq $line) { break }
        Write-Output "{`"error`":`"$errMsg`"}"
    }
    exit 0
}

$mgr = $initBox['mgr']

function Poll-Json {
    $pb = [hashtable]::Synchronized(@{ result = '{}' })
    Run-OnSta -timeoutMs 5000 -shared $pb -sb {
        param($s)
        Add-Type -AssemblyName System.Runtime.WindowsRuntime
        $session = $args[0]  # won't work — use closure via param
        # mgr is in parent scope, access via $using: not available in Thread
        # Pass mgr through shared hashtable
        $m = $s['mgr']
        $session = $m.GetCurrentSession()
        if ($null -eq $session) { $s['result'] = '{}'; return }

        $mpOp = $session.TryGetMediaPropertiesAsync()
        $tlOp = $session.GetTimelinePropertiesAsync()
        $pbOp = $session.GetPlaybackInfoAsync()

        function Spin($op) {
            $d = [DateTime]::UtcNow.AddSeconds(3)
            while ([int]$op.Status -eq 0 -and [DateTime]::UtcNow -lt $d) { [System.Threading.Thread]::Sleep(10) }
            if ([int]$op.Status -eq 1) { return $op.GetResults() }
            return $null
        }

        $mp = Spin $mpOp
        $tl = Spin $tlOp
        $pb = Spin $pbOp

        $title  = if ($null -eq $mp) { '' } else { [string]$mp.Title }
        $artist = if ($null -eq $mp) { '' } else { [string]$mp.Artist }
        $appId  = [string]$session.SourceAppUserModelId
        $pos    = if ($null -eq $tl) { 0 } else { [int64]$tl.Position.TotalMilliseconds }
        $dur    = if ($null -eq $tl) { 0 } else { [int64]$tl.EndTime.TotalMilliseconds - [int64]$tl.StartTime.TotalMilliseconds }
        $state  = if ($null -eq $pb) { 0 } else { [int]$pb.PlaybackStatus }

        $s['result'] = ([ordered]@{
            title = $title; artist = $artist; appId = $appId
            positionMs = $pos; durationMs = $dur; state = $state
        } | ConvertTo-Json -Compress)
    }
    $pb['mgr'] = $mgr
    return $pb['result']
}

# Oops — Run-OnSta starts thread before we set mgr. Fix: pass mgr in shared from start.
function Poll-Json {
    $shared = [hashtable]::Synchronized(@{ mgr = $mgr; result = '{}' })
    $t = [System.Threading.Thread]::new([System.Threading.ParameterizedThreadStart]{
        param($s)
        try {
            Add-Type -AssemblyName System.Runtime.WindowsRuntime
            $m = $s['mgr']
            $session = $m.GetCurrentSession()
            if ($null -eq $session) { return }

            function Spin($op) {
                $d = [DateTime]::UtcNow.AddSeconds(3)
                while ([int]$op.Status -eq 0 -and [DateTime]::UtcNow -lt $d) { [System.Threading.Thread]::Sleep(10) }
                if ([int]$op.Status -eq 1) { return $op.GetResults() }
                return $null
            }

            $mp    = Spin ($session.TryGetMediaPropertiesAsync())
            $tl    = Spin ($session.GetTimelinePropertiesAsync())
            $pb    = Spin ($session.GetPlaybackInfoAsync())
            $title  = if ($null -eq $mp) { '' } else { [string]$mp.Title }
            $artist = if ($null -eq $mp) { '' } else { [string]$mp.Artist }
            $appId  = [string]$session.SourceAppUserModelId
            $pos    = if ($null -eq $tl) { 0 } else { [int64]$tl.Position.TotalMilliseconds }
            $dur    = if ($null -eq $tl) { 0 } else { [int64]$tl.EndTime.TotalMilliseconds - [int64]$tl.StartTime.TotalMilliseconds }
            $state  = if ($null -eq $pb) { 0 } else { [int]$pb.PlaybackStatus }
            $s['result'] = ([ordered]@{
                title = $title; artist = $artist; appId = $appId
                positionMs = $pos; durationMs = $dur; state = $state
            } | ConvertTo-Json -Compress)
        } catch {
            $s['result'] = "{`"error`":`"$($_.Exception.Message)`"}"
        }
    })
    $t.SetApartmentState([System.Threading.ApartmentState]::STA)
    $t.IsBackground = $true
    $t.Start($shared)
    $null = $t.Join(5000)
    return $shared['result']
}

function Do-Skip([string]$dir) {
    $shared = [hashtable]::Synchronized(@{ mgr = $mgr; dir = $dir })
    $t = [System.Threading.Thread]::new([System.Threading.ParameterizedThreadStart]{
        param($s)
        try {
            $session = $s['mgr'].GetCurrentSession()
            if ($null -eq $session) { return }
            $op = if ($s['dir'] -eq 'NEXT') { $session.TrySkipNextAsync() } else { $session.TrySkipPreviousAsync() }
            $d = [DateTime]::UtcNow.AddSeconds(3)
            while ([int]$op.Status -eq 0 -and [DateTime]::UtcNow -lt $d) { [System.Threading.Thread]::Sleep(10) }
        } catch {}
    })
    $t.SetApartmentState([System.Threading.ApartmentState]::STA)
    $t.IsBackground = $true
    $t.Start($shared)
    $null = $t.Join(3000)
    return 'OK'
}

while ($true) {
    $line = [Console]::In.ReadLine()
    if ($null -eq $line) { break }
    switch ($line.Trim()) {
        'POLL'      { Write-Output (Poll-Json) }
        'SKIP_NEXT' { Write-Output (Do-Skip 'NEXT') }
        'SKIP_PREV' { Write-Output (Do-Skip 'PREV') }
        default     { Write-Output '{}' }
    }
}
