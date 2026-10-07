public class MemoryTest {
    public static void main(String[] args) {
        System.out.println("===== MEMORY TEST CASES =====");
        Memory m = new Memory();

        TestSupport.check("M1  power-up: all 256 cells are 0", allZero(m));
        m.write(0x30, 0x5A);
        TestSupport.check("M2  write then read back 0x5A at 30H", m.read(0x30) == 0x5A);
        m.write(0x00, 0x11);
        m.write(0xFF, 0x22);
        TestSupport.check("M3  boundary addresses 00H and FFH", m.read(0x00) == 0x11 && m.read(0xFF) == 0x22);
        m.write(0x31, 0x1FF);
        TestSupport.check("M4  values are truncated to 8 bits (1FFH -> FFH)", m.read(0x31) == 0xFF);
        m.write(0x32, -1);
        TestSupport.check("M5  negative value stored as 8-bit (-1 -> FFH)", m.read(0x32) == 0xFF);
        m.write(0x40, 1);
        m.write(0x40, 2);
        TestSupport.check("M6  overwrite replaces old value", m.read(0x40) == 2);
        TestSupport.check("M7  neighbouring cells unaffected", m.read(0x3F) == 0 && m.read(0x41) == 0);
        TestSupport.expectThrows("M8  read at 100H is rejected", () -> new Memory().read(0x100));
        TestSupport.expectThrows("M9  write at -1 is rejected", () -> new Memory().write(-1, 5));
        m.clear();
        TestSupport.check("M10 clear() zeroes memory", allZero(m));

        Memory s = new Memory();
        s.write(5, 1); s.write(6, 2); s.read(5); s.read(6); s.read(6);
        TestSupport.check("M11 access counters (2 writes, 3 reads)", s.getWriteCount() == 2 && s.getReadCount() == 3);
        TestSupport.check("M12 last read/write address tracked", s.getLastReadAddress() == 6 && s.getLastWriteAddress() == 6);
        s.peek(5); s.poke(7, 9);
        TestSupport.check("M13 peek/poke do not change counters", s.getWriteCount() == 2 && s.getReadCount() == 3);
        TestSupport.check("M14 dump() shows hex row", s.dump(0x00, 0x07).contains("00: 00 00 00 00 00 01 02 09"));

        // ---- integration with the CPU ----
        CPU cpu = TestSupport.runAsm(
            "MOV 30H,#55H\n" +        // direct write
            "MOV A,30H\n" +           // direct read
            "MOV R0,#31H\n" +
            "MOV @R0,A\n" +           // indirect write
            "MOV R1,#31H\n" +
            "MOV B,@R1\n" +           // indirect read into B
            "MOV R3,#7\n");           // register lives in memory
        TestSupport.check("M15 CPU direct write: MOV 30H,#55H", cpu.getMemory().peek(0x30) == 0x55);
        TestSupport.check("M16 CPU direct read:  MOV A,30H", cpu.getA() == 0x55);
        TestSupport.check("M17 CPU indirect write: MOV @R0,A", cpu.getMemory().peek(0x31) == 0x55);
        TestSupport.check("M18 CPU indirect read:  MOV B,@R1", cpu.getB() == 0x55);
        TestSupport.check("M19 R3 is stored at address 03H", cpu.getMemory().peek(3) == 7);
        cpu = TestSupport.runAsm("MOV 05H,#9\nMOV A,R5\n");
        TestSupport.check("M20 writing address 05H changes R5", cpu.getA() == 9);
        cpu = TestSupport.runAsm("MOV R0,#0FFH\nMOV @R0,#77H\nMOV A,@R0\n");
        TestSupport.check("M21 indirect access to top of RAM (FFH)", cpu.getA() == 0x77 && cpu.getMemory().peek(0xFF) == 0x77);
        TestSupport.check("M22 memory loop: fill 40H-47H with 0..7",
            fillTest());

        TestSupport.summary("MEMORY TEST");
    }

    private static boolean fillTest() {
        CPU cpu = TestSupport.runAsm(
            "      MOV R0,#40H\n" +
            "      MOV R2,#8\n" +
            "      MOV A,#0\n" +
            "LOOP: MOV @R0,A\n" +
            "      INC A\n" +
            "      INC R0\n" +
            "      DJNZ R2,LOOP\n");
        for (int i = 0; i < 8; i++) if (cpu.getMemory().peek(0x40 + i) != i) return false;
        return cpu.getMemory().peek(0x48) == 0;
    }

    private static boolean allZero(Memory m) {
        for (int i = 0; i < Memory.SIZE; i++) if (m.peek(i) != 0) return false;
        return true;
    }
}
