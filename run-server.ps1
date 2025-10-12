# MOEM Server Start Script (PowerShell)
param(
    [int]$Port = 8080,
    [int]$TimeoutSeconds = 300
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "MOEM Server Start Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Write-Host "Checking if port $Port is in use..." -ForegroundColor Yellow

# Check if port is in use
$portInUse = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue

if ($portInUse) {
    Write-Host "Port $Port is already in use!" -ForegroundColor Red
    
    # Get the process using the port
    $process = Get-Process -Id $portInUse.OwningProcess -ErrorAction SilentlyContinue
    
    if ($process) {
        Write-Host "Found process: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Yellow
        Write-Host "Killing process..." -ForegroundColor Yellow
        
        try {
            Stop-Process -Id $process.Id -Force
            Write-Host "Process killed successfully." -ForegroundColor Green
            Start-Sleep -Seconds 2
        }
        catch {
            Write-Host "Failed to kill process: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "Please kill the process manually and try again." -ForegroundColor Red
            Read-Host "Press Enter to continue..."
            exit 1
        }
    }
    else {
        Write-Host "Could not find the process using port $Port" -ForegroundColor Red
        Write-Host "Please kill the process manually and try again." -ForegroundColor Red
        Read-Host "Press Enter to continue..."
        exit 1
    }
}
else {
    Write-Host "Port $Port is available." -ForegroundColor Green
}

Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "Timeout set to $TimeoutSeconds seconds" -ForegroundColor Yellow

# Start the application
try {
    $process = Start-Process -FilePath ".\gradlew.bat" -ArgumentList "bootRun", "--no-daemon", "--info" -NoNewWindow -PassThru
    
    # Wait for the process to start or timeout
    $timeout = (Get-Date).AddSeconds($TimeoutSeconds)
    
    while (-not $process.HasExited -and (Get-Date) -lt $timeout) {
        Start-Sleep -Seconds 5
        
        # Check if the application is responding
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$Port/api/user-profiles" -Method GET -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "Application started successfully!" -ForegroundColor Green
                Write-Host "Server is running on http://localhost:$Port" -ForegroundColor Green
                break
            }
        }
        catch {
            # Application not ready yet, continue waiting
        }
    }
    
    if ($process.HasExited) {
        Write-Host "Application process has exited." -ForegroundColor Red
        Write-Host "Exit code: $($process.ExitCode)" -ForegroundColor Red
    }
    elseif ((Get-Date) -ge $timeout) {
        Write-Host "Application startup timed out after $TimeoutSeconds seconds." -ForegroundColor Red
        Write-Host "The application might still be starting up in the background." -ForegroundColor Yellow
    }
}
catch {
    Write-Host "Failed to start application: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "Script completed." -ForegroundColor Cyan
Read-Host "Press Enter to exit..."
