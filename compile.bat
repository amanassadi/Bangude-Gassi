@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if not exist out mkdir out
set SOURCES=
for /R src %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
javac -d out !SOURCES!
exit /b %errorlevel%
