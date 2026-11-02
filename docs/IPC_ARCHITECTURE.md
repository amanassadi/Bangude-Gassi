# Week 4 IPC Architecture

```text
+------------------+       POSIX FIFO        +-----------------------------+
|   UI PROCESS     | ----------------------> |        CORE PROCESS         |
| Swing controls   |   /tmp/stc89_ui_to_core| CPU + Memory + Stack + FIFO |
| status/display   | <---------------------- |                             |
+------------------+       POSIX FIFO        +-------------+---------------+
          ^                    /tmp/stc89_core_to_ui       |
          |                                                | POSIX FIFO
          |                                                | /tmp/stc89_core_to_log
          |                                                v
          |                                      +------------------+
          +--------------------------------------| LOGGING PROCESS  |
                                                 | execution.log   |
                                                 +------------------+
```

## Selected IPC: POSIX Named Pipes (FIFO)

We use **POSIX named pipes (FIFOs)** because the Week 4 goal is to make the UI,
Core and Logging components independent OS processes while keeping the message
flow simple.

- UI -> Core: commands (`RUN|...`, `EXIT`)
- Core -> UI: status/results (`STATUS|...`, `REGS|...`, `DONE|...`)
- Core -> Logger: execution/error events (`INFO|...`, `ERROR|...`)

A FIFO is a kernel-managed byte stream represented by a filesystem path.
Different processes can open the same FIFO and communicate without sharing Java
objects or memory.

## Why not shared memory?

Shared memory is faster for large/high-frequency data, but it adds synchronization
complexity. Our simulator mainly exchanges short command, status and log messages,
so FIFO is easier to demonstrate and explain.

## Important platform note

`mkfifo` and POSIX FIFOs are intended for Linux/Unix. On Windows, run the Week 4
project through WSL (Ubuntu) or another POSIX-compatible environment.
