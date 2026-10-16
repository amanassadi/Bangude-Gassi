# STC89C52 Simulator - Design Document (updated for Week 3)

Role covered: **CPU and instructions**.

## 1. Architecture overview

```
 programs/*.asm --> Assembler --> List<Instruction> --> CPU (fetch-decode-execute)
                                                          |
                         +--------------------------------+---------------------+
                         |                                |                     |
                      Memory (256 B IRAM)        Stack (SP, in IRAM)     FIFO queue (in IRAM)
                      R0-R7 @ 00H-07H            08H upward              HEAD/TAIL/COUNT/STATUS + buffer
                         |                                |                     |
                         +------------------ ConsoleView (registers, memory, stack, queue) ---+
```

| File | Responsibility |
|---|---|
| `Instruction.java` | Decoded instruction: opcode, destination, operand, target (3rd operand for CJNE) |
| `CPU.java` | Registers, flags, SP/PC, fetch-decode-execute, all instructions, PUSH/POP |
| `Memory.java` | 256-byte internal RAM with checked read/write, access statistics, hex dump |
| `Assembler.java` | Two-pass assembler: labels, `EQU` constants, comments, error messages with line numbers |
| `FifoQueue.java` | Circular-buffer FIFO stored in Memory (Enqueue, Dequeue, empty/full, status) |
| `ConsoleView.java` | User interface: memory dump, stack + SP, queue contents and status |
| `QueueValidator.java` | Expected-vs-actual comparison for the assembly queue program |
| `Main.java` | Runs an assembly file and shows registers, memory, stack and queue |

## 2. Changes to the Week 2 CPU

| Area | Week 2 | Week 3 |
|---|---|---|
| R0-R7 | separate Java fields | stored in Memory bank 0 (00H-07H), so direct address `03H` *is* R3 |
| Addressing modes | register, immediate | + direct (`30H`), register-indirect (`@R0`, `@R1`) |
| Stack | `SP` field only | real stack in IRAM: PUSH/POP, ACALL/RET, overflow and underflow detection |
| Branches | `SJMP` only | + `LJMP`, `JZ`, `JNZ`, `JC`, `JNC`, `CJNE`, `DJNZ` |
| Instructions | MOV ADD SUBB INC DEC ANL ORL SJMP | + XRL CLR SETB CPL NOP HALT PUSH POP ACALL LCALL RET |
| Parity flag | updated by some instructions | follows A after every instruction (real 8051 behaviour) |
| Input | Java `new Instruction(...)` objects | also text assembly via `Assembler` |
| Safety | none | cycle limit (1,000,000) stops runaway programs |
| `Instruction.toString()` | printed `SJMP` without operand | prints `SJMP 5` |

Week 2 tests (`CPU_Test`) still pass unchanged.

## 3. Memory design

* 256 bytes, addresses 00H-FFH, values always truncated to 8 bits.
* `read`/`write` are the bus accesses (counted, last address tracked for the UI); `peek`/`poke` are silent.
* Out-of-range addresses throw `IllegalArgumentException`.
* Direct addresses 00H-7FH go to RAM. A few SFRs are mapped: `0E0H` = A, `0F0H` = B, `81H` = SP. Other SFRs are not modelled and raise an error.
* `@R0`/`@R1` can reach the whole 256 bytes.

Map used by the programs:

| Range | Use |
|---|---|
| 00H-07H | R0-R7 (bank 0) |
| 08H-1FH | stack (grows upward, SP starts at 07H) |
| 20H-23H | queue control: HEAD, TAIL, COUNT, STATUS |
| 30H-37H | queue buffer (8 slots) |
| 40H-4FH | output area (dequeued values) |
| 60H-65H | recorded STATUS / COUNT results for validation |

## 4. Stack design

* `SP` resets to 07H. **PUSH** = `SP = SP + 1; IRAM[SP] = value`. **POP** = `value = IRAM[SP]; SP = SP - 1`.
* POP at SP = 07H throws *Stack underflow*; PUSH at SP = FFH throws *Stack overflow* (248 bytes of stack available).
* `ACALL`/`LCALL` push the 16-bit return address (low byte, then high byte); `RET` pops high then low - same order as the real 8051.
* `PUSH`/`POP` accept A, B, R0-R7, SP, or a direct address.
* UI: `ConsoleView.showStack()` prints SP, depth, and each stack byte with the top marked.

## 5. FIFO queue design

Circular buffer in memory (capacity 8, a power of two so wrap-around is `AND 07H`):

| Address | Name | Meaning |
|---|---|---|
| 20H | HEAD | index of oldest element |
| 21H | TAIL | index of next free slot |
| 22H | COUNT | number of stored elements |
| 23H | STATUS | 0 = OK, 1 = FULL, 2 = EMPTY (result of last operation) |
| 30H-37H | BUFFER | data slots |

* **Enqueue:** if COUNT = 8 -> STATUS = FULL, nothing changes. Else `BUFFER[TAIL] = value; TAIL = (TAIL+1) mod 8; COUNT++; STATUS = OK`.
* **Dequeue:** if COUNT = 0 -> STATUS = EMPTY, nothing removed. Else `value = BUFFER[HEAD]; HEAD = (HEAD+1) mod 8; COUNT--; STATUS = OK`.
* Empty is `COUNT = 0`, full is `COUNT = 8`. Using COUNT avoids the classic HEAD = TAIL ambiguity.
* The queue is implemented **twice with the same layout**: in assembly (`ENQ`/`DEQ` in `queue_test.asm`, running on the simulated CPU) and in Java (`FifoQueue`). This lets the Java model act as a reference for the assembly.
* Flowchart: `docs/queue_flowchart.png`.
* UI: `ConsoleView.showQueue()` shows count/capacity, HEAD and TAIL, status, EMPTY/FULL markers, every slot (live vs stale) and the FIFO order.

## 6. Assembly program design (`programs/queue_test.asm`)

* Subroutines `QINIT`, `ENQ` (value in R2), `DEQ` (result in R3); both save and restore R0 with PUSH/POP and use ACALL/RET, so the stack is exercised too.
* `ENQ` uses `CJNE A,#QCAP,...` for the full check; `DEQ` uses `JNZ` for the empty check.
* The main program uses `DJNZ` loops, `@R1` indirect stores, direct-to-direct `MOV`, and `ADD` to generate values.
* Final check: SP = 07H proves PUSH/POP and CALL/RET are balanced.

## 7. Known limitations

* `PC` is a program *index*, not a byte address; `SJMP`/`CJNE` etc. take absolute indexes (labels are resolved by the assembler).
* Only register bank 0 is modelled; bit-addressable RAM, timers, ports and most SFRs are not part of this project phase.
* `CLR`, `SETB`, `CPL` support A and C only; `MUL`, `DIV`, `MOVC`, `MOVX`, `RL`/`RR`, `ADDC` are not implemented.
* The queue is a fixed-size array of 8 bytes by design (real embedded style).
