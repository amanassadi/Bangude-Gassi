import java.util.ArrayList;
import java.util.List;

public class Integration_Test {
    private static int passed, failed;

    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println(name + " : PASS");
            passed++;
        } else {
            System.out.println(name + " : FAIL");
            failed++;
        }
    }

    public static void main(String[] args) {
        CPU cpu = new CPU();

        cpu.getMemory().write(30, 10);
        cpu.getMemory().write(31, 20);

        check("CPU-Memory Read",
                cpu.getMemory().read(30) == 10 &&
                cpu.getMemory().read(31) == 20);

        cpu.execute(new Instruction("PUSH", "", "#10"));
        cpu.execute(new Instruction("PUSH", "", "#20"));
        cpu.execute(new Instruction("POP", "R0", ""));

        check("CPU-Stack Integration",
                cpu.getRegisterValue("R0") == 20);

        List<Instruction> program = new ArrayList<>();

        program.add(new Instruction("ENQUEUE", "", "#10"));
        program.add(new Instruction("ENQUEUE", "", "#20"));
        program.add(new Instruction("ENQUEUE", "", "#30"));
        program.add(new Instruction("DEQUEUE", "R1", ""));
        program.add(new Instruction("DEQUEUE", "R2", ""));
        program.add(new Instruction("DEQUEUE", "R3", ""));

        cpu.loadProgram(program);
        cpu.run();

        check("CPU-Queue FIFO",
                cpu.getRegisterValue("R1") == 10 &&
                cpu.getRegisterValue("R2") == 20 &&
                cpu.getRegisterValue("R3") == 30);

        System.out.println("\n===== INTEGRATION TEST SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0)
            System.exit(1);
    }
}
