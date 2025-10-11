@echo off
setlocal

echo ==========================================
echo MOEM Server Start Script
echo ==========================================

set PORT=8080
set TIMEOUT_SECONDS=300

echo Checking if port %PORT% is in use...

:: Check if port is in use
netstat -ano | findstr :%PORT% >nul
if %errorlevel% equ 0 (
    echo Port %PORT% is already in use!
    echo Finding process using port %PORT%...
    
    :: Get the PID of the process using the port
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT%') do (
        set PID=%%a
        goto :found
    )
    
    :found
    if defined PID (
        echo Killing process with PID %PID%...
        taskkill /PID %PID% /F
        if %errorlevel% equ 0 (
            echo Process killed successfully.
            timeout /t 2 /nobreak >nul
        ) else (
            echo Failed to kill process. Please kill it manually.
            pause
            exit /b 1
        )
    )
) else (
    echo Port %PORT% is available.
)

echo Starting Spring Boot application...
echo Timeout set to %TIMEOUT_SECONDS% seconds

:: Start the application with timeout
timeout /t 1 /nobreak >nul
gradlew.bat bootRun --no-daemon --info

if %errorlevel% neq 0 (
    echo Application failed to start!
    echo Please check the logs above for errors.
    pause
    exit /b 1
)

echo Application started successfully!
pause
