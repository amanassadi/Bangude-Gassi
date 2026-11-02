public class Stack_Test {
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
        Stack stack = new Stack();

        stack.push(10);
        stack.push(20);
        stack.push(30);

        check("Stack PUSH", stack.getSP() == 10);
        check("Stack POP", stack.pop() == 30);
        check("LIFO Order", stack.pop() == 20);
        check("SP After POP", stack.getSP() == 8);

        System.out.println("\n===== STACK TEST SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0)
            System.exit(1);
    }
}
