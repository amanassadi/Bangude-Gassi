@echo off
REM Compile everything, run the demo and all test programs (Windows).
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\*.java || exit /b 1
javac -cp out -d out test\*.java || exit /b 1

java -cp out Main > out\queue-execution-results.txt
echo Demo run saved to out\queue-execution-results.txt

type nul > out\test-results-week3.txt
for %%T in (CPU_Test InstructionTest MemoryTest StackTest QueueTest QueueAsmTest) do (
    java -cp out %%T >> out\test-results-week3.txt
    if errorlevel 1 echo %%T FAILED
)
type out\test-results-week3.txt
