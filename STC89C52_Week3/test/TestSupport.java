import java.util.List;

/** Tiny PASS/FAIL helper shared by the Week 3 test programs. */
public class TestSupport {
    private static int passed = 0;
    private static int failed = 0;

    public static void check(String name, boolean condition) {
        if (condition) { System.out.println(name + " : PASS"); passed++; }
        else           { System.out.println(name + " : FAIL"); failed++; }
    }

    public static void expectThrows(String name, Runnable action) {
        try {
            action.run();
            check(name, false);
        } catch (RuntimeException e) {
            check(name + "  [" + e.getMessage() + "]", true);
        }
    }

    /** Assemble and run a program given as text; returns the finished CPU. */
    public static CPU runAsm(String source) {
        List<Instruction> program = Assembler.assemble(source);
        CPU cpu = new CPU();
        cpu.loadProgram(program);
        cpu.setTrace(false);
        cpu.run();
        return cpu;
    }

    public static void summary(String title) {
        System.out.println();
        System.out.println("===== " + title + " SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}
