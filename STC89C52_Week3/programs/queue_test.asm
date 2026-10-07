; =====================================================================
;  queue_test.asm  -  FIFO queue validation program for the STC89C52
;                     (8051 instruction set) simulator - Week 3
;
;  Queue: circular buffer of 8 bytes in internal RAM.
;     HEAD   (20H) index of oldest element
;     TAIL   (21H) index of next free slot
;     COUNT  (22H) number of elements stored
;     STATUS (23H) 0 = OK, 1 = FULL, 2 = EMPTY (result of last operation)
;     QBUF   (30H-37H) data slots
;
;  Subroutines
;     QINIT : head = tail = count = status = 0
;     ENQ   : enqueue value in R2.   Sets STATUS (1 if queue was full)
;     DEQ   : dequeue into R3.       Sets STATUS (2 if queue was empty)
;     Both save/restore R0 with PUSH/POP and use the stack for the
;     return address (ACALL/RET).
;
;  Test scenario (expected results are in the documentation)
;     1. Enqueue 11H,22H,33H,44H,55H             -> COUNT = 5
;     2. Dequeue 3 values  -> 11H,22H,33H        -> COUNT = 2
;     3. Enqueue 66H..0BBH (6 values, wraps)     -> COUNT = 8 (FULL)
;     4. Enqueue 0CCH on a full queue            -> rejected, STATUS = 1
;     5. Dequeue 8 values -> 44H..0BBH           -> COUNT = 0 (EMPTY)
;     6. Dequeue on empty queue                  -> STATUS = 2
;
;  Results written for checking:
;     40H-4AH  the 11 dequeued values, in the order they came out
;     60H      STATUS after the enqueue on a full queue   (expect 01H)
;     61H      STATUS after the dequeue on an empty queue (expect 02H)
;     62H-65H  COUNT after step 1,2,3,5                   (expect 5,2,8,0)
; =====================================================================

HEAD      EQU 20H
TAIL      EQU 21H
COUNT     EQU 22H
STATUS    EQU 23H
QBUF      EQU 30H
QCAP      EQU 8            ; capacity (power of two so ANL can wrap)
QMASK     EQU 07H          ; QCAP - 1
OUTBUF    EQU 40H
S_OK      EQU 0
S_FULL    EQU 1
S_EMPTY   EQU 2

; ---------------------------- main program ---------------------------
START:    ACALL QINIT

; ---- step 1: five explicit enqueues
          MOV R2,#11H
          ACALL ENQ
          MOV R2,#22H
          ACALL ENQ
          MOV R2,#33H
          ACALL ENQ
          MOV R2,#44H
          ACALL ENQ
          MOV R2,#55H
          ACALL ENQ
          MOV 62H,COUNT           ; expect 5

; ---- step 2: dequeue three (loop with DJNZ)
          MOV R1,#OUTBUF          ; R1 = output pointer
          MOV R5,#3
DLOOP1:   ACALL DEQ
          MOV A,R3
          MOV @R1,A               ; store dequeued value
          INC R1
          DJNZ R5,DLOOP1
          MOV 63H,COUNT           ; expect 2

; ---- step 3: enqueue 66H,77H,88H,99H,0AAH,0BBH (loop, wraps around)
          MOV R2,#66H
          MOV R5,#6
ELOOP:    ACALL ENQ
          MOV A,R2
          ADD A,#11H              ; next value
          MOV R2,A
          DJNZ R5,ELOOP
          MOV 64H,COUNT           ; expect 8 (full)

; ---- step 4: enqueue on a full queue must be rejected
          MOV R2,#0CCH
          ACALL ENQ
          MOV 60H,STATUS          ; expect 1 (FULL)

; ---- step 5: dequeue all eight
          MOV R5,#8
DLOOP2:   ACALL DEQ
          MOV A,R3
          MOV @R1,A
          INC R1
          DJNZ R5,DLOOP2
          MOV 65H,COUNT           ; expect 0

; ---- step 6: dequeue on an empty queue must report EMPTY
          ACALL DEQ
          MOV 61H,STATUS          ; expect 2 (EMPTY)
          HALT

; ---------------------------- QINIT ----------------------------------
QINIT:    MOV HEAD,#0
          MOV TAIL,#0
          MOV COUNT,#0
          MOV STATUS,#S_OK
          RET

; ---------------------------- ENQ ------------------------------------
; in : R2 = value        out: STATUS (0 ok / 1 full)      uses A, R0
ENQ:      PUSH R0
          MOV A,COUNT
          CJNE A,#QCAP,ENQ_GO     ; count != capacity -> there is room
          MOV STATUS,#S_FULL      ; full: reject, queue unchanged
          SJMP ENQ_END
ENQ_GO:   MOV A,#QBUF
          ADD A,TAIL              ; A = address of slot [tail]
          MOV R0,A
          MOV A,R2
          MOV @R0,A               ; buffer[tail] = value
          MOV A,TAIL
          INC A
          ANL A,#QMASK            ; tail = (tail + 1) mod 8
          MOV TAIL,A
          INC COUNT
          MOV STATUS,#S_OK
ENQ_END:  POP R0
          RET

; ---------------------------- DEQ ------------------------------------
; out: R3 = value, STATUS (0 ok / 2 empty)               uses A, R0
DEQ:      PUSH R0
          MOV A,COUNT
          JNZ DEQ_GO              ; count != 0 -> something to remove
          MOV STATUS,#S_EMPTY     ; empty: nothing removed, R3 unchanged
          SJMP DEQ_END
DEQ_GO:   MOV A,#QBUF
          ADD A,HEAD              ; A = address of slot [head]
          MOV R0,A
          MOV A,@R0
          MOV R3,A                ; result = buffer[head]
          MOV A,HEAD
          INC A
          ANL A,#QMASK            ; head = (head + 1) mod 8
          MOV HEAD,A
          DEC COUNT
          MOV STATUS,#S_OK
DEQ_END:  POP R0
          RET
