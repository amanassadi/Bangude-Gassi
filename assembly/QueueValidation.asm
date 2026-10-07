; STC89C52 Week 3 - FIFO Queue Validation
; The simulator's Instruction layer represents these queue operations.

ORG 0000H

; Enqueue: 10H, 20H, 30H
MOV A,#10H
MOV 30H,A
MOV A,#20H
MOV 31H,A
MOV A,#30H
MOV 32H,A

; Expected FIFO dequeue order: 10H, 20H, 30H
MOV A,30H
MOV 40H,A
MOV A,31H
MOV 41H,A
MOV A,32H
MOV 42H,A

END
