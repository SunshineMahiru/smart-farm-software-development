$ErrorActionPreference = 'Stop'

$root = 'D:\smart-farm-software-development'
$serverDir = Join-Path $root 'packages\server'
$webDir = Join-Path $root 'packages\web'
$logDir = Join-Path $root 'scripts\logs'
$serverOutLog = Join-Path $logDir 'server.out.log'
$serverErrLog = Join-Path $logDir 'server.err.log'
$webOutLog = Join-Path $logDir 'web.out.log'
$webErrLog = Join-Path $logDir 'web.err.log'
$backendUrl = 'http://localhost:8080'
$frontendPortStart = 5173
$frontendPortEnd = 5180
$mysqlPort = 3306
$defaultLoginPayload = @{
    username = 'admin01'
    password = '123456'
} | ConvertTo-Json -Compress

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
    $ports = $frontendPortStart..$frontendPortEnd
    foreach ($port in $ports) {
        if (Test-PortListening $port) {
            return "http://localhost:$port"
        }
    }
    return $null
}

function Reset-LogFile([string]$path) {
    if (Test-Path -LiteralPath $path) {
        Clear-Content -LiteralPath $path
    } else {
        New-Item -ItemType File -Path $path | Out-Null
    }
}

function Invoke-HealthRequest([string]$url, [string]$method = 'Get', [string]$body = $null) {
    $params = @{
        Uri = $url
        Method = $method
        ContentType = 'application/json'
        TimeoutSec = 5
        ErrorAction = 'Stop'
    }
    if ($null -ne $body) {
        $params.Body = $body
    }
    try {
        return Invoke-RestMethod @params
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            return $null
        }
        throw
    }
}

function Test-BackendLogin {
    try {
        $response = Invoke-HealthRequest -url "$backendUrl/sys/user/login" -method 'Post' -body $defaultLoginPayload
        if ($null -eq $response) {
            return $false
        }
        return $response.code -eq 200 -and $null -ne $response.data -and $null -ne $response.data.token
    } catch {
        return $false
    }
}

function Wait-ForBackendLogin([int]$timeoutSeconds) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-BackendLogin) {
            return $true
        }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Get-LatestLogSnippet([string]$path, [int]$lineCount = 30) {
    if (-not (Test-Path -LiteralPath $path)) {
        return $null
    }
    $content = Get-Content -LiteralPath $path -Tail $lineCount -ErrorAction SilentlyContinue
    if ($null -eq $content -or $content.Count -eq 0) {
        return $null
    }
    return ($content -join [Environment]::NewLine)
}

Ensure-Directory $logDir
Reset-LogFile $serverOutLog
Reset-LogFile $serverErrLog
Reset-LogFile $webOutLog
Reset-LogFile $webErrLog

if (-not (Test-PortListening $mysqlPort)) {
    Write-Host "MySQL is not listening on port $mysqlPort. Start MySQL first, then import packages/server/db/init_schema.sql into database smart_farm."
    exit 1
}

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

$backendReady = Wait-ForPort -port 8080 -timeoutSeconds 60
if (-not $backendReady) {
    Write-Host ''
    Write-Host "Backend did not open port 8080 within 60 seconds. Check $serverOutLog and $serverErrLog"
    exit 1
}

$backendLoginReady = Wait-ForBackendLogin -timeoutSeconds 45
if (-not $backendLoginReady) {
    Write-Host ''
    Write-Host 'Backend port is open, but login is still unavailable.'
    Write-Host 'Common causes: MySQL password mismatch, smart_farm database not imported, or MySQL 8 auth plugin mismatch.'
    $serverSnippet = Get-LatestLogSnippet -path $serverOutLog
    if ($serverSnippet) {
        Write-Host ''
        Write-Host 'Recent backend log:'
        Write-Host $serverSnippet
    }
    exit 1
}

Write-Host 'Backend login check passed.'

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

$frontendReady = Wait-ForPort -port $frontendPortStart -timeoutSeconds 30
if (-not $frontendReady) {
    $frontendUrl = Get-FrontendUrl
    $frontendReady = $null -ne $frontendUrl
} else {
    $frontendUrl = "http://localhost:$frontendPortStart"
}

Write-Host ''
Write-Host "Backend URL:  $backendUrl"

if ($frontendReady -and $frontendUrl) {
    Write-Host "Frontend URL: $frontendUrl"
} else {
    Write-Host "Frontend did not become ready within 30 seconds. Check $webOutLog and $webErrLog"
}

Write-Host "Server out:    $serverOutLog"
Write-Host "Server err:    $serverErrLog"
Write-Host "Frontend out:  $webOutLog"
Write-Host "Frontend err:  $webErrLog"
