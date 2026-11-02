# Instruction Set Reference - STC89C52 Simulator (Week 3)

Operand notation: `Rn` = R0-R7, `#n` immediate, `dir` direct address (e.g. `30H`), `@Ri` = `@R0`/`@R1`, `label` = branch target.
Numbers: decimal `10`, hex `1AH` or `0x1A` (hex starting with a letter needs a leading 0, e.g. `0AAH`).

| Instruction | Operation | Flags | Week |
|---|---|---|---|
| `MOV dest,src` | dest = src (dest: A, B, Rn, dir, @Ri; src: any incl. `#n`) | P follows A | 2 / 3 |
| `MOV DPTR,#n16` | load 16-bit DPTR | - | 3 |
| `ADD A,src` | A = A + src | CY AC OV P | 2 / 3 |
| `SUBB A,src` | A = A - src - CY | CY AC OV P | 2 / 3 |
| `INC x` / `DEC x` | x = x +/- 1 (x: A, Rn, dir, @Ri; `INC DPTR`) | P | 2 / 3 |
| `ANL`, `ORL`, `XRL dest,src` | bitwise AND / OR / XOR | P | 2 / 3 |
| `CLR A`, `CPL A` | A = 0 / A = NOT A | P | 3 |
| `CLR C`, `SETB C`, `CPL C` | clear / set / complement carry | CY | 3 |
| `NOP` | no operation | - | 3 |
| `PUSH x` | SP = SP+1; IRAM[SP] = x | - | 3 |
| `POP x` | x = IRAM[SP]; SP = SP-1 | P | 3 |
| `ACALL label` / `LCALL label` | push return address (2 bytes), jump | - | 3 |
| `RET` | pop return address, jump back | - | 3 |
| `SJMP label` / `LJMP label` | unconditional jump | - | 2 / 3 |
| `JZ label` / `JNZ label` | jump if A = 0 / A != 0 | - | 3 |
| `JC label` / `JNC label` | jump if CY = 1 / CY = 0 | - | 3 |
| `CJNE x,y,label` | compare x,y; jump if x != y; CY = (x < y) | CY | 3 |
| `DJNZ x,label` | x = x-1; jump if x != 0 | - | 3 |
| `HALT` | simulator pseudo-instruction: stop the CPU (real 8051 uses `SJMP $`) | - | 3 |

Assembler directives: `NAME EQU value`, `label:`, `; comment`.
Errors: unknown mnemonic, wrong operand count, undefined label, duplicate label, bad operand, writing to an immediate, `@R2`-`@R7`, stack overflow/underflow, memory address out of range.
