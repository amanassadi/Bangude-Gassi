# STC89C52 Simulator - Week 3

Educational Java simulator for the STC89C52 (8051 core).
**Week 3: CPU + Memory + Stack + FIFO Queue**, validated with a queue flowchart, a processor-specific assembly program and documented tests.
Role covered in this package: **CPU and instructions**.

## Folder structure

```text
STC89C52_Week3/
├── src/        CPU, Instruction, Memory, Assembler, FifoQueue, ConsoleView, QueueValidator, Main
├── programs/   queue_test.asm  (assembly validation program)
├── test/       CPU_Test (Week 2 regression), InstructionTest, MemoryTest, StackTest, QueueTest, QueueAsmTest
├── docs/       DESIGN_DOCUMENT.md, TEST_DOCUMENT.md, INSTRUCTION_SET.md, queue_flowchart.(png|svg|dot)
├── out/        compiled classes + saved results (test-results-week3.txt, queue-execution-results.txt)
├── run_all.sh  compile + demo + all tests (Linux/macOS)
└── run_all.bat compile + demo + all tests (Windows)
```

## Build, run, test

Requires a JDK (11 or newer). From this folder:

```bash
./run_all.sh                         # everything (Windows: run_all.bat)

javac -d out src/*.java              # compile only
javac -cp out -d out test/*.java
java -cp out Main                    # run programs/queue_test.asm, show registers/memory/stack/queue
java -cp out Main --trace            # same, with fetch/decode/execute trace
java -cp out Main myprogram.asm      # run any assembly file
java -cp out InstructionTest         # or MemoryTest, StackTest, QueueTest, QueueAsmTest, CPU_Test
```

## Week 3 deliverables checklist

| Requirement | Where |
|---|---|
| Memory read/write, integrated, shown in UI | `Memory.java`, `ConsoleView.showMemory`, `MemoryTest` |
| Stack Pointer, PUSH/POP, shown in UI | `CPU.java`, `ConsoleView.showStack`, `StackTest` |
| FIFO queue: Enqueue/Dequeue, FIFO order, shown in UI | `FifoQueue.java` + `ENQ`/`DEQ` in assembly, `ConsoleView.showQueue`, `QueueTest` |
| Queue flowchart (enqueue, dequeue, empty, full, status) | `docs/queue_flowchart.png` |
| Assembly validation program | `programs/queue_test.asm` |
| Execution results, expected vs actual | `out/queue-execution-results.txt`, `docs/TEST_DOCUMENT.md` section 7 |
| Memory / stack / queue test cases | `docs/TEST_DOCUMENT.md` sections 1-4 |
| Updated design documentation | `docs/DESIGN_DOCUMENT.md`, `docs/INSTRUCTION_SET.md` |

## Results

Last run: all suites pass (112 test cases) and all 30 expected-vs-actual queue checks pass.
The queue program executes 482 instructions and ends with SP = 07H, HEAD = TAIL = 3, COUNT = 0 and the dequeued values
`11 22 33 44 55 66 77 88 99 AA BB` in exact FIFO order.

## Notes for team integration

`Memory`, `FifoQueue` and `ConsoleView` are included so the CPU can be demonstrated end to end. If teammates own the memory, queue or UI
parts, their classes can replace these as long as they keep the same interface: `CPU.getMemory()`, `read/write/peek/poke`, and the queue layout
(HEAD 20H, TAIL 21H, COUNT 22H, STATUS 23H, buffer 30H-37H).
