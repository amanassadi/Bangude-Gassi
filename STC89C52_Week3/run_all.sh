#!/bin/bash
# Compile everything, run the demo and all test programs, save results to out/.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out src/*.java
javac -cp out -d out test/*.java

java -cp out Main > out/queue-execution-results.txt
echo "Demo run saved to out/queue-execution-results.txt"

: > out/test-results-week3.txt
fail=0
for t in CPU_Test InstructionTest MemoryTest StackTest QueueTest QueueAsmTest; do
    java -cp out $t | tee -a out/test-results-week3.txt || fail=1
    echo | tee -a out/test-results-week3.txt
done
[ $fail -eq 0 ] && echo "ALL TEST SUITES PASSED" || { echo "SOME TESTS FAILED"; exit 1; }
