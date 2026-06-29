$ErrorActionPreference = 'Stop'

$root = 'D:\smart-farm-software-development'
$serverDir = Join-Path $root 'packages\server'
$webDir = Join-Path $root 'packages\web'
$logDir = Join-Path $root 'scripts\logs'
$serverOutLog = Join-Path $logDir 'server.out.log'
$serverErrLog = Join-Path $logDir 'server.err.log'
$webOutLog = Join-Path $logDir 'web.out.log'
$webErrLog = Join-Path $logDir 'web.err.log'

function Ensure-Directory([string]$path) {
    if (-not (Test-Path -LiteralPath $path)) {
        New-Item -ItemType Directory -Path $path | Out-Null
    }
}

function Test-PortListening([int]$port) {
    $connection = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
        Where-Object { $_.LocalPort -eq $port } |
        Select-Object -First 1
    return $null -ne $connection
}

function Wait-ForPort([int]$port, [int]$timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortListening $port) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }
    return $false
}

function Get-FrontendUrl {
    $ports = 5173..5180
    foreach ($port in $ports) {
        if (Test-PortListening $port) {
            return "http://localhost:$port"
        }
    }
    return $null
}

Ensure-Directory $logDir

if (-not (Test-PortListening 8080)) {
    Start-Process -FilePath 'cmd.exe' `
        -ArgumentList '/c', 'mvn spring-boot:run' `
        -WorkingDirectory $serverDir `
        -RedirectStandardOutput $serverOutLog `
        -RedirectStandardError $serverErrLog `
        -WindowStyle Hidden
    Write-Host 'Starting backend...'
} else {
    Write-Host 'Backend is already running on port 8080.'
}

$frontendUrl = Get-FrontendUrl
if (-not $frontendUrl) {
    Start-Process -FilePath 'cmd.exe' `
        -ArgumentList '/c', 'npm run dev' `
        -WorkingDirectory $webDir `
        -RedirectStandardOutput $webOutLog `
        -RedirectStandardError $webErrLog `
        -WindowStyle Hidden
    Write-Host 'Starting frontend...'
} else {
    Write-Host "Frontend is already running at $frontendUrl"
}

$backendReady = Wait-ForPort -port 8080 -timeoutSeconds 60
$frontendReady = Wait-ForPort -port 5173 -timeoutSeconds 30

if (-not $frontendReady) {
    $frontendUrl = Get-FrontendUrl
    $frontendReady = $null -ne $frontendUrl
} else {
    $frontendUrl = 'http://localhost:5173'
}

Write-Host ''
if ($backendReady) {
    Write-Host 'Backend URL:  http://localhost:8080'
} else {
    Write-Host "Backend did not become ready within 60 seconds. Check $serverOutLog and $serverErrLog"
}

if ($frontendReady -and $frontendUrl) {
    Write-Host "Frontend URL: $frontendUrl"
} else {
    Write-Host "Frontend did not become ready within 30 seconds. Check $webOutLog and $webErrLog"
}

Write-Host "Server out:    $serverOutLog"
Write-Host "Server err:    $serverErrLog"
Write-Host "Frontend out:  $webOutLog"
Write-Host "Frontend err:  $webErrLog"
