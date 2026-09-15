public class Memory_Test {
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
        Memory memory = new Memory();

        memory.write(10, 50);
        check("Memory Write/Read", memory.read(10) == 50);

        memory.write(20, 255);
        check("8-Bit Value", memory.read(20) == 255);

        System.out.println("\n===== MEMORY TEST SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0)
            System.exit(1);
    }
}
