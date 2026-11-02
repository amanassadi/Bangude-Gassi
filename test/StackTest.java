public class StackTest {
    public static void main(String[] args) {
        System.out.println("===== STACK TEST CASES =====");

        CPU cpu = new CPU();
        TestSupport.check("S1  SP = 07H after reset", cpu.getSP() == 0x07);
        TestSupport.check("S2  stack empty after reset", cpu.getStackDepth() == 0);

        cpu = TestSupport.runAsm("MOV A,#0AAH\nPUSH A\n");
        TestSupport.check("S3  PUSH increments SP (07H -> 08H)", cpu.getSP() == 0x08);
        TestSupport.check("S4  PUSH stores value at [SP]", cpu.getMemory().peek(0x08) == 0xAA);

        cpu = TestSupport.runAsm("MOV A,#1\nPUSH A\nMOV A,#2\nPUSH A\nMOV A,#3\nPUSH A\n");
        int[] st = cpu.getStackContents();
        TestSupport.check("S5  three pushes -> depth 3, SP=0AH", cpu.getStackDepth() == 3 && cpu.getSP() == 0x0A);
        TestSupport.check("S6  stack contents bottom->top = 1,2,3", st.length == 3 && st[0] == 1 && st[1] == 2 && st[2] == 3);

        cpu = TestSupport.runAsm(
            "MOV A,#1\nPUSH A\nMOV A,#2\nPUSH A\nMOV A,#3\nPUSH A\n" +
            "POP R5\nPOP R6\nPOP R7\n");
        TestSupport.check("S7  LIFO: pops return 3,2,1", cpu.getRegisterValue("R5") == 3
                && cpu.getRegisterValue("R6") == 2 && cpu.getRegisterValue("R7") == 1);
        TestSupport.check("S8  SP back to 07H after popping all", cpu.getSP() == 0x07);

        cpu = TestSupport.runAsm("MOV 30H,#42H\nPUSH 30H\nPOP 31H\n");
        TestSupport.check("S9  PUSH/POP with direct addresses", cpu.getMemory().peek(0x31) == 0x42);

        cpu = TestSupport.runAsm("MOV A,#9\nMOV B,#5\nPUSH A\nPUSH B\nPOP A\nPOP B\n");
        TestSupport.check("S10 swap A and B using the stack", cpu.getA() == 5 && cpu.getB() == 9);

        TestSupport.expectThrows("S11 POP on empty stack -> underflow", () -> TestSupport.runAsm("POP A\n"));

        TestSupport.expectThrows("S12 overflow: 249th PUSH exceeds FFH", () -> {
            CPU c = new CPU();
            for (int i = 0; i < 249; i++) c.push(i);
        });
        cpu = new CPU();
        for (int i = 0; i < 248; i++) cpu.push(i);
        TestSupport.check("S13 248 pushes fill stack exactly to SP=FFH", cpu.getSP() == 0xFF);

        // subroutine call / return
        cpu = TestSupport.runAsm(
            "      MOV A,#1\n" +
            "      ACALL SUB\n" +
            "      MOV R7,#99\n" +
            "      HALT\n" +
            "SUB:  ADD A,#10\n" +
            "      RET\n");
        TestSupport.check("S14 ACALL/RET runs subroutine and returns", cpu.getA() == 11 && cpu.getRegisterValue("R7") == 99);
        TestSupport.check("S15 SP restored after RET", cpu.getSP() == 0x07);

        cpu = TestSupport.runAsm(
            "      ACALL SUB\n" +
            "      HALT\n" +
            "SUB:  MOV R6,SP\n" +
            "      RET\n");
        TestSupport.check("S16 return address uses 2 stack bytes (SP=09H inside call)", cpu.getRegisterValue("R6") == 0x09);

        cpu = TestSupport.runAsm(
            "      ACALL OUTER\n" +
            "      HALT\n" +
            "OUTER: ACALL INNER\n" +
            "      MOV R1,SP\n" +
            "      RET\n" +
            "INNER: MOV R0,SP\n" +
            "      RET\n");
        TestSupport.check("S17 nested calls: depth 4 inside INNER (SP=0BH)", cpu.getRegisterValue("R0") == 0x0B);
        TestSupport.check("S18 back in OUTER depth 2 (SP=09H), final SP=07H",
                cpu.getRegisterValue("R1") == 0x09 && cpu.getSP() == 0x07);

        TestSupport.expectThrows("S19 RET with empty stack -> underflow", () -> TestSupport.runAsm("RET\n"));

        cpu = TestSupport.runAsm("MOV R0,#12H\nPUSH R0\nMOV R0,#0\nPOP R0\n");
        TestSupport.check("S20 PUSH/POP saves and restores a register", cpu.getRegisterValue("R0") == 0x12);

        TestSupport.summary("STACK TEST");
    }
}
