@echo off
setlocal
cd /d "%~dp0"
call compile.bat
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
if exist execution.log del execution.log
java -cp out IntegrationLauncher
