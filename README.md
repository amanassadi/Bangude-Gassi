# STC89C52 Week 4 — Multi-Process Simulator & IPC

## Processes
- UI Process: Swing interface and user interaction.
- Core Process: CPU execution, Memory, Stack and FIFO Queue.
- Logging Process: execution/error logging.
- Integration: creates FIFOs and starts the three independent processes.

## POSIX IPC
**POSIX Named Pipes (FIFO)** are used for communication between processes.

- UI -> Core: `/tmp/stc89_ui_to_core`
- Core -> UI: `/tmp/stc89_core_to_ui`
- Core -> Logger: `/tmp/stc89_core_to_log`

POSIX threads are used where concurrency is appropriate inside a process. They are
not the IPC mechanism; FIFOs provide the inter-process communication.

## Flow
`UI -> Core -> CPU / Memory / Stack / Queue -> Logger`

## Folder structure
- `core/` — Week 3 CPU, Instruction, Memory, Stack, FifoQueue, Assembler + CoreProcess
- `ui/` — UIProcess
- `logging/` — LoggingProcess
- `integration/` — IntegrationLauncher
- `tests/` — IPC FIFO smoke test
- `programs/` — sample assembly program
- `docs/` — architecture, IPC justification and benchmark template

## Linux / WSL run
From the Week 4 directory:

```bash
chmod +x run_week4.sh
./run_week4.sh
```

The launcher creates the three POSIX FIFOs and starts Logging, Core and UI.

For the FIFO smoke test, while the launcher is running:

```bash
java -cp tests IPCTest
```

Expected result:
`IPC FIFO TEST PASSED`

## Demo
1. Run `./run_week4.sh`.
2. The Swing UI opens.
3. Keep `programs/queue_test.asm` in the project directory, or enter another assembly path.
4. Click **Run**.
5. UI sends `RUN|program.asm` to Core.
6. Core assembles and executes using CPU/Memory/Stack/FifoQueue.
7. Core sends status/results to UI.
8. Core sends INFO/ERROR messages to Logger.
9. Logger appends them to `execution.log`.
10. Click **Exit** in the UI to stop the Core process.

## Platform note
The POSIX FIFO implementation requires Linux/Unix. On Windows, use WSL/Ubuntu for the Week 4 demonstration.
