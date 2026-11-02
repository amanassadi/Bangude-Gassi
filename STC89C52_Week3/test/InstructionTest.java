public class InstructionTest {
    private static CPU run(String src) { return TestSupport.runAsm(src); }

    public static void main(String[] args) {
        System.out.println("===== NEW INSTRUCTION TEST CASES (Week 3) =====");
        CPU c;

        // ---- MOV addressing modes
        c = run("MOV R2,#12H\nMOV A,R2\n");
        TestSupport.check("I1  MOV A,Rn", c.getA() == 0x12);
        c = run("MOV 40H,#7\nMOV 41H,40H\n");
        TestSupport.check("I2  MOV direct,direct", c.getMemory().peek(0x41) == 7);
        c = run("MOV DPTR,#1234H\n");
        TestSupport.check("I3  MOV DPTR,#16-bit", c.getDPTR() == 0x1234);
        c = run("MOV DPTR,#0FFFFH\nINC DPTR\n");
        TestSupport.check("I4  INC DPTR wraps to 0000H", c.getDPTR() == 0);
        TestSupport.expectThrows("I5  MOV to immediate rejected", () -> run("MOV #5,A\n"));
        TestSupport.expectThrows("I6  @R2 rejected (only @R0/@R1)", () -> run("MOV A,@R2\n"));

        // ---- INC / DEC on memory
        c = run("MOV 30H,#0FFH\nINC 30H\n");
        TestSupport.check("I7  INC direct wraps FFH -> 00H", c.getMemory().peek(0x30) == 0);
        c = run("MOV R0,#30H\nMOV 30H,#5\nINC @R0\nINC @R0\nDEC @R0\n");
        TestSupport.check("I8  INC/DEC @R0", c.getMemory().peek(0x30) == 6);
        c = run("MOV A,#0\nDEC A\n");
        TestSupport.check("I9  DEC A wraps 00H -> FFH", c.getA() == 0xFF);

        // ---- arithmetic / logic with memory operands
        c = run("MOV 30H,#20\nMOV A,#5\nADD A,30H\n");
        TestSupport.check("I10 ADD A,direct", c.getA() == 25);
        c = run("MOV R0,#30H\nMOV @R0,#3\nMOV A,#10\nCLR C\nSUBB A,@R0\n");
        TestSupport.check("I11 SUBB A,@R0", c.getA() == 7);
        c = run("MOV A,#0FFH\nANL A,#0F0H\nXRL A,#0FFH\n");
        TestSupport.check("I12 ANL then XRL", c.getA() == 0x0F);
        c = run("MOV 30H,#0F0H\nORL 30H,#0FH\n");
        TestSupport.check("I13 ORL direct,#imm", c.getMemory().peek(0x30) == 0xFF);
        c = run("MOV A,#0AAH\nCPL A\n");
        TestSupport.check("I14 CPL A (AAH -> 55H)", c.getA() == 0x55);
        c = run("MOV A,#9\nCLR A\n");
        TestSupport.check("I15 CLR A", c.getA() == 0);
        c = run("SETB C\n");
        TestSupport.check("I16 SETB C", c.getCY());
        c = run("SETB C\nCLR C\n");
        TestSupport.check("I17 CLR C", !c.getCY());
        c = run("SETB C\nCPL C\n");
        TestSupport.check("I18 CPL C", !c.getCY());
        c = run("NOP\nNOP\nMOV A,#1\n");
        TestSupport.check("I19 NOP changes nothing", c.getA() == 1);

        // ---- parity follows A on every instruction (real 8051 behaviour)
        c = run("MOV A,#07H\n");
        TestSupport.check("I20 P=1 after MOV A,#07H (3 ones)", c.getP());
        c = run("MOV A,#03H\n");
        TestSupport.check("I21 P=0 after MOV A,#03H (2 ones)", !c.getP());

        // ---- conditional jumps
        c = run("MOV A,#0\nJZ SKIP\nMOV R7,#1\nSKIP: MOV R6,#2\n");
        TestSupport.check("I22 JZ taken when A=0", c.getRegisterValue("R7") == 0 && c.getRegisterValue("R6") == 2);
        c = run("MOV A,#1\nJZ SKIP\nMOV R7,#1\nSKIP: MOV R6,#2\n");
        TestSupport.check("I23 JZ not taken when A!=0", c.getRegisterValue("R7") == 1);
        c = run("MOV A,#1\nJNZ SKIP\nMOV R7,#1\nSKIP: MOV R6,#2\n");
        TestSupport.check("I24 JNZ taken when A!=0", c.getRegisterValue("R7") == 0);
        c = run("MOV A,#255\nADD A,#1\nJC SKIP\nMOV R7,#1\nSKIP: MOV R6,#2\n");
        TestSupport.check("I25 JC taken after carry", c.getRegisterValue("R7") == 0);
        c = run("MOV A,#1\nADD A,#1\nJNC SKIP\nMOV R7,#1\nSKIP: MOV R6,#2\n");
        TestSupport.check("I26 JNC taken when no carry", c.getRegisterValue("R7") == 0);

        c = run("MOV A,#5\nCJNE A,#5,NE\nMOV R7,#1\nNE: MOV R6,#2\n");
        TestSupport.check("I27 CJNE equal -> not taken, CY=0", c.getRegisterValue("R7") == 1 && !c.getCY());
        c = run("MOV A,#3\nCJNE A,#5,NE\nMOV R7,#1\nNE: MOV R6,#2\n");
        TestSupport.check("I28 CJNE A<imm -> taken, CY=1", c.getRegisterValue("R7") == 0 && c.getCY());
        c = run("MOV A,#9\nCJNE A,#5,NE\nMOV R7,#1\nNE: MOV R6,#2\n");
        TestSupport.check("I29 CJNE A>imm -> taken, CY=0", c.getRegisterValue("R7") == 0 && !c.getCY());
        c = run("MOV R3,#4\nCJNE R3,#4,NE\nMOV R7,#1\nNE: MOV R6,#2\n");
        TestSupport.check("I30 CJNE Rn,#imm", c.getRegisterValue("R7") == 1);

        c = run("MOV R2,#5\nMOV A,#0\nL: ADD A,#2\nDJNZ R2,L\n");
        TestSupport.check("I31 DJNZ loop runs 5 times (A=10, R2=0)", c.getA() == 10 && c.getRegisterValue("R2") == 0);
        c = run("MOV 30H,#3\nMOV A,#0\nL: INC A\nDJNZ 30H,L\n");
        TestSupport.check("I32 DJNZ direct (3 iterations)", c.getA() == 3);
        c = run("MOV R2,#0\nMOV A,#0\nL: INC A\nDJNZ R2,L\n");
        TestSupport.check("I33 DJNZ with 0 wraps to FFH (256 iterations)", c.getRegisterValue("R2") == 0 && c.getA() == 0);

        c = run("SJMP END\nMOV A,#9\nEND: MOV R1,#1\n");
        TestSupport.check("I34 SJMP to label skips code", c.getA() == 0 && c.getRegisterValue("R1") == 1);
        c = run("LJMP END\nMOV A,#9\nEND: MOV R1,#1\n");
        TestSupport.check("I35 LJMP to label", c.getA() == 0);
        c = run("MOV A,#2\nMOV R1,#0\nHALT\nMOV R1,#5\n");
        TestSupport.check("I36 HALT stops execution", c.isHalted() && c.getRegisterValue("R1") == 0);

        // ---- assembler / safety
        TestSupport.expectThrows("I37 unknown mnemonic rejected", () -> run("FOO A,#1\n"));
        TestSupport.expectThrows("I38 undefined label rejected", () -> run("SJMP NOWHERE\n"));
        TestSupport.expectThrows("I39 wrong operand count rejected", () -> run("MOV A\n"));
        TestSupport.expectThrows("I40 infinite loop caught by cycle limit", () -> run("L: SJMP L\n"));
        c = run("DATA EQU 30H\nVAL EQU 0AH\nMOV DATA,#VAL\nMOV A,DATA\n");
        TestSupport.check("I41 EQU constants (#VAL, DATA)", c.getA() == 10);
        TestSupport.check("I42 Instruction.toString() prints SJMP operand", new Instruction("SJMP", "", "5").toString().equals("SJMP 5"));

        TestSupport.summary("INSTRUCTION TEST");
    }
}
