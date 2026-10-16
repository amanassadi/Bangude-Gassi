# Week 4 IPC Architecture and Justification

## Selected mechanism
Java TCP sockets.

## Why
The team needs three independent Java processes to communicate on a Windows development machine. TCP sockets are available directly through standard Java (`java.net`) and do not require Linux/WSL. The protocol is intentionally small and line-based.

## Message flow
1. UI connects to Core on port 5000.
2. UI sends `LOADSRC`, `STEP`, `RUN`, `RESET` or `PING`.
3. Core executes the existing Week 3 CPU/Memory/Stack/FIFO Queue code.
4. Core sends state snapshots back to UI.
5. Core connects to Logger on port 5001 and sends INFO/ERROR messages.
6. Logger appends messages to `execution.log`.

## POSIX note
TCP sockets are IPC, but they are not a POSIX IPC primitive such as POSIX FIFO, POSIX shared memory or POSIX message queues. If the course requires the word “POSIX” literally, the team should select POSIX FIFO and run that implementation in Linux/WSL. This Windows build intentionally uses Java TCP sockets for portability.
