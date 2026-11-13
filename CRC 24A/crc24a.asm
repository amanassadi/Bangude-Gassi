import java.util.List;

public class CRC24ATest {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "programs/crc24a_test.asm";
        List<Instruction> program = Assembler.assembleFile(path);
        CPU cpu = new CPU();
        cpu.setTrace(false);
        cpu.loadProgram(program);
        cpu.run();

        Memory memory = cpu.getMemory();
        int generated = (memory.peek(0x59) << 16)
                      | (memory.peek(0x5A) << 8)
                      | memory.peek(0x5B);
        int remainder = (memory.peek(0x2A) << 16)
                      | (memory.peek(0x2B) << 8)
                      | memory.peek(0x2C);

        System.out.printf("Generated CRC-24A: %06X (reference: CDE703)%n", generated);
        System.out.printf("Receiver remainder: %06X (expected: 000000)%n", remainder);

        if (generated == 0xCDE703 && remainder == 0) {
            System.out.println("CRC-24A ASSEMBLY TEST PASSED");
        } else {
            System.out.println("CRC-24A ASSEMBLY TEST NEEDS CORRECTION");
        }
    }
  }
