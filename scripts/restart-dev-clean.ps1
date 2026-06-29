$ErrorActionPreference = 'Stop'

$root = 'D:\smart-farm-software-development'
$ensureMysqlScript = Join-Path $root 'scripts\ensure-mysql.ps1'
$startDevScript = Join-Path $root 'scripts\start-dev.ps1'
$portsToClear = @(8080) + (5173..5180)

function Get-ListeningPids([int]$port) {
    return @(Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
        Where-Object { $_.LocalPort -eq $port } |
        Select-Object -ExpandProperty OwningProcess -Unique)
}

function Stop-PortProcesses([int]$port) {
    $owningProcesses = @(Get-ListeningPids $port | Where-Object { $_ -and $_ -ne 0 } | Sort-Object -Unique)
    if ($owningProcesses.Count -eq 0) {
        Write-Host "Port $port is already free."
        return $true
    }

    foreach ($processId in $owningProcesses) {
        if ($processId -eq 4) {
            Write-Host "Port $port is occupied by PID 4 (System). Release it manually before starting the project."
            return $false
        }

        try {
            $process = Get-Process -Id $processId -ErrorAction Stop
            Write-Host "Stopping PID $processId ($($process.ProcessName)) on port $port..."
            Stop-Process -Id $processId -Force -ErrorAction Stop
        } catch {
            Write-Host "Failed to stop PID $processId on port ${port}: $($_.Exception.Message)"
            return $false
        }
    }

    Start-Sleep -Seconds 1
    return (Get-ListeningPids $port).Count -eq 0
}

foreach ($port in $portsToClear) {
    if (-not (Stop-PortProcesses $port)) {
        Write-Host "Port cleanup failed for $port."
        exit 1
    }
}

& $ensureMysqlScript
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& $startDevScript
exit $LASTEXITCODE
