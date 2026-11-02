@echo off
setlocal
cd /d "%~dp0"
call compile.bat
if errorlevel 1 exit /b 1
if exist execution.log del execution.log
start "W4 Logger" cmd /c "cd /d \"%~dp0\" && java -cp out LoggingProcess"
timeout /t 1 /nobreak >nul
start "W4 Core" cmd /c "cd /d \"%~dp0\" && java -cp out CoreProcess"
timeout /t 1 /nobreak >nul
java -cp out IPCIntegrationTest
if errorlevel 1 (
  echo IPC test failed.
  exit /b 1
)
echo.
echo ALL WEEK 4 IPC TESTS PASSED
