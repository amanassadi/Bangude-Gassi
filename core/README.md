# STC89C52 Week 4 - Multi-Process Simulator & IPC

This project is built directly on the supplied Week 3 simulator.

## Week 3 retained
- CPU
- Instruction
- Memory
- Stack
- FifoQueue
- Assembler
- QueueValidator
- Existing dark-blue Swing SimulatorUI
- Week 3 tests and documentation

## Week 4 added
- UI Process
- Core Process
- Logging Process
- IntegrationLauncher
- Java TCP socket IPC
- Threaded client handling with ExecutorService
- IPCIntegrationTest
- Windows BAT launch/test scripts

## Architecture
UI Process --TCP :5000--> Core Process --TCP :5001--> Logging Process

Core contains the existing Week 3 CPU, Memory, Stack, FIFO Queue and Assembler.

## Windows run
1. Install JDK 11 or newer.
2. Open Command Prompt in this project folder.
3. Run `run_week4.bat`.
4. The existing Week 3 UI opens.
5. Use LOAD and RUN as in Week 3.

## IPC test
Run `test_week4.bat`.
Expected final line:
ALL WEEK 4 IPC TESTS PASSED

## Important IPC note
This Windows version uses Java TCP sockets. TCP sockets are not technically a
POSIX IPC primitive. The Week 4 brief requires selecting an IPC mechanism but
does not name FIFO as mandatory. If the instructor explicitly requires POSIX
IPC, use the FIFO implementation under Linux/WSL.
