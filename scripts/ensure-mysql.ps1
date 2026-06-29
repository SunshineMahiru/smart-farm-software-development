$ErrorActionPreference = 'Stop'

$mysqlPort = 3306
$serviceCandidates = @('MySQL80', 'MySQL', 'mysql', 'MariaDB')

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
        Start-Sleep -Seconds 1
    }
    return $false
}

function Try-StartService([string]$serviceName) {
    $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
    if ($null -eq $service) {
        return $false
    }

    Write-Host "Found MySQL service: $($service.Name) [$($service.Status)]"
    if ($service.Status -ne 'Running') {
        try {
            Start-Service -Name $service.Name -ErrorAction Stop
            Write-Host "Starting service $($service.Name)..."
        } catch {
            Write-Host "Failed to start service $($service.Name): $($_.Exception.Message)"
            return $false
        }
    }

    return $true
}

if (Test-PortListening $mysqlPort) {
    Write-Host "MySQL is already listening on port $mysqlPort."
    exit 0
}

foreach ($serviceName in $serviceCandidates) {
    if (-not (Try-StartService $serviceName)) {
        continue
    }

    if (Wait-ForPort -port $mysqlPort -timeoutSeconds 25) {
        Write-Host "MySQL is ready on port $mysqlPort."
        exit 0
    }
}

Write-Host "MySQL is still not listening on port $mysqlPort."
$relatedServices = Get-Service -ErrorAction SilentlyContinue |
    Where-Object {
        $_.Name -like '*mysql*' -or
        $_.DisplayName -like '*mysql*' -or
        $_.Name -like '*maria*' -or
        $_.DisplayName -like '*maria*'
    } |
    Select-Object Name, Status, DisplayName

if ($relatedServices) {
    Write-Host 'Found database-related services:'
    $relatedServices | Format-Table -AutoSize
} else {
    Write-Host 'No common MySQL service was found. Start your MySQL service manually first.'
}

exit 1
