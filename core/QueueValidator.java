import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Validates the assembly queue program (programs/queue_test.asm) by comparing
 * what the simulated CPU actually produced in memory with what two independent
 * reference models say it should produce:
 *
 *   1. java.util.ArrayDeque        - the textbook FIFO behaviour
 *   2. FifoQueue (Java)            - same circular-buffer layout as the assembly
 *
 * Produces an "expected vs actual" table (the Week 3 deliverable).
 */
public class QueueValidator {

    public static final int OUT_BASE = 0x40;

    private final List<String> rows = new ArrayList<>();
    private int passed;
    private int failed;

    public int getPassed() { return passed; }
    public int getFailed() { return failed; }
    public List<String> getRows() { return rows; }

    private void compare(String check, int expected, int actual) {
        boolean ok = expected == actual;
        if (ok) passed++; else failed++;
        rows.add(String.format("%-44s %02XH        %02XH        %s",
                check, expected, actual, ok ? "PASS" : "FAIL"));
    }

    /** Runs the reference models and compares them with the CPU's memory. */
    public void validate(CPU cpu) {
        Memory actual = cpu.getMemory();

        // ---- reference model 1: ArrayDeque (pure FIFO semantics) ----------
        Deque<Integer> ref = new ArrayDeque<>();
        List<Integer> expectedOut = new ArrayList<>();
        int[] expectedCounts = new int[4];
        int expectedStatusFull;
        int expectedStatusEmpty;

        // ---- reference model 2: FifoQueue on a private memory -------------
        Memory refMem = new Memory();
        FifoQueue q = new FifoQueue(refMem);

        int[] first = {0x11, 0x22, 0x33, 0x44, 0x55};
        for (int v : first) { ref.addLast(v); q.enqueue(v); }
        expectedCounts[0] = ref.size();
        for (int i = 0; i < 3; i++) { expectedOut.add(ref.removeFirst()); q.dequeue(); }
        expectedCounts[1] = ref.size();
        for (int i = 0, v = 0x66; i < 6; i++, v += 0x11) { ref.addLast(v); q.enqueue(v); }
        expectedCounts[2] = ref.size();
        // enqueue on full queue: model must reject
        boolean accepted = ref.size() < FifoQueue.CAPACITY;
        if (accepted) ref.addLast(0xCC);
        q.enqueue(0xCC);
        expectedStatusFull = q.getStatus();
        for (int i = 0; i < 8; i++) { expectedOut.add(ref.removeFirst()); q.dequeue(); }
        expectedCounts[3] = ref.size();
        q.dequeue();                       // dequeue on empty
        expectedStatusEmpty = q.getStatus();

        // ---- compare ------------------------------------------------------
        rows.add(String.format("%-44s %-10s %-10s %s", "CHECK", "EXPECTED", "ACTUAL", "RESULT"));
        rows.add("-----------------------------------------------------------------------------------");

        for (int i = 0; i < expectedOut.size(); i++) {
            compare(String.format("Dequeue #%d value (FIFO order) @%02XH", i + 1, OUT_BASE + i),
                    expectedOut.get(i), actual.peek(OUT_BASE + i));
        }
        String[] stage = {"after 5 enqueues", "after 3 dequeues", "after 6 more enqueues (full)", "after 8 dequeues (empty)"};
        for (int i = 0; i < 4; i++) {
            compare("COUNT " + stage[i], expectedCounts[i], actual.peek(0x62 + i));
        }
        compare("STATUS: enqueue on FULL queue (1=FULL)", expectedStatusFull, actual.peek(0x60));
        compare("STATUS: dequeue on EMPTY queue (2=EMPTY)", expectedStatusEmpty, actual.peek(0x61));
        compare("Rejected value 0CCH never entered queue", 0,
                countOccurrences(actual, 0xCC));

        // final queue structure must be identical to the Java FifoQueue
        compare("Final HEAD index (wrap-around)", refMem.peek(FifoQueue.HEAD_ADDR), actual.peek(FifoQueue.HEAD_ADDR));
        compare("Final TAIL index (wrap-around)", refMem.peek(FifoQueue.TAIL_ADDR), actual.peek(FifoQueue.TAIL_ADDR));
        compare("Final COUNT", refMem.peek(FifoQueue.COUNT_ADDR), actual.peek(FifoQueue.COUNT_ADDR));
        for (int i = 0; i < FifoQueue.CAPACITY; i++) {
            compare("Buffer slot " + i + " @" + String.format("%02XH", FifoQueue.BUFFER_BASE + i),
                    refMem.peek(FifoQueue.BUFFER_BASE + i), actual.peek(FifoQueue.BUFFER_BASE + i));
        }
        compare("Stack empty after program (SP = 07H)", CPU.STACK_BASE, cpu.getSP());
    }

    private static int countOccurrences(Memory m, int value) {
        int n = 0;
        for (int a = FifoQueue.BUFFER_BASE; a < FifoQueue.BUFFER_BASE + FifoQueue.CAPACITY; a++) {
            if (m.peek(a) == value) n++;
        }
        return n;
    }

    public String report() {
        StringBuilder sb = new StringBuilder();
        for (String r : rows) sb.append(r).append('\n');
        sb.append("-----------------------------------------------------------------------------------\n");
        sb.append(String.format("Total checks: %d   Passed: %d   Failed: %d%n", passed + failed, passed, failed));
        return sb.toString();
    }
}
