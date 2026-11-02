import java.io.IOException;
import java.util.List;

/**
 * STC89C52 simulator - Week 3 demo.
 *
 *   java -cp out Main [program.asm] [--trace]
 *
 * Default program: programs/queue_test.asm. After the program finishes the
 * registers, memory, stack and FIFO queue are displayed, followed by the
 * expected-vs-actual validation table (only for the queue program).
 */
public class Main {
    public static void main(String[] args) throws IOException {
        String path = "programs/queue_test.asm";
        boolean trace = false;
        for (String a : args) {
            if (a.equals("--trace")) trace = true;
            else path = a;
        }

        List<Instruction> program = Assembler.assembleFile(path);
        CPU cpu = new CPU();
        cpu.loadProgram(program);
        cpu.setTrace(trace);

        System.out.println("===== STC89C52 WEEK 3 SIMULATOR =====");
        System.out.println("Program : " + path + "  (" + program.size() + " instructions)");

        cpu.run();

        System.out.println("Halted  : " + cpu.isHalted() + "   instructions executed: " + cpu.getCycles());
        System.out.println();
        ConsoleView.showRegisters(cpu);
        System.out.println();
        ConsoleView.showMemory(cpu, 0x00, 0x0F, "register bank 0 + stack area");
        System.out.println();
        ConsoleView.showMemory(cpu, 0x20, 0x37, "queue control + buffer");
        System.out.println();
        ConsoleView.showMemory(cpu, 0x40, 0x4F, "output area");
        System.out.println();
        ConsoleView.showMemory(cpu, 0x60, 0x67, "status / count records");
        System.out.println();
        ConsoleView.showStack(cpu);
        System.out.println();
        ConsoleView.showQueue(cpu.getMemory());

        if (path.endsWith("queue_test.asm")) {
            System.out.println();
            System.out.println("========== QUEUE VALIDATION: EXPECTED vs ACTUAL ==========");
            QueueValidator v = new QueueValidator();
            v.validate(cpu);
            System.out.print(v.report());
            if (v.getFailed() > 0) System.exit(1);
        }
    }
}
