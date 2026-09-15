public class Queue_Test {
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
        FIFOQueue queue = new FIFOQueue(5);

        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        check("Queue Enqueue", queue.size() == 3);
        check("FIFO First", queue.dequeue() == 10);
        check("FIFO Second", queue.dequeue() == 20);
        check("FIFO Third", queue.dequeue() == 30);
        check("Queue Empty", queue.isEmpty());

        System.out.println("\n===== QUEUE TEST SUMMARY =====");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0)
            System.exit(1);
    }
}
