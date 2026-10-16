public class CPU_Test {
    private static int passed = 0;
    private static int failed = 0;

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
        CPU cpu;

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#10"));
        check("MOV", cpu.getA() == 10);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#10"));
        cpu.execute(new Instruction("ADD", "A", "#5"));
        check("ADD", cpu.getA() == 15);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#10"));
        cpu.execute(new Instruction("SUBB", "A", "#3"));
        check("SUBB", cpu.getA() == 7);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "R0", "#10"));
        cpu.execute(new Instruction("INC", "R0", ""));
        check("INC", cpu.getRegisterValue("R0") == 11);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#10"));
        cpu.execute(new Instruction("DEC", "A", ""));
        check("DEC", cpu.getA() == 9);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#15"));
        cpu.execute(new Instruction("ANL", "A", "#3"));
        check("ANL", cpu.getA() == 3);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#5"));
        cpu.execute(new Instruction("ORL", "A", "#2"));
        check("ORL", cpu.getA() == 7);

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#255"));
        cpu.execute(new Instruction("ADD", "A", "#1"));
        check("8-BIT OVERFLOW", cpu.getA() == 0 && cpu.getCY());

        cpu = new CPU();
        cpu.execute(new Instruction("MOV", "A", "#5"));
        cpu.execute(new Instruction("SJMP", "", "0"));
        check("SJMP", cpu.getPC() == 0);

        System.out.println();
        System.out.println("===== TEST SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }
}
