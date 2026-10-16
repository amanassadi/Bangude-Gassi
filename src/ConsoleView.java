/**
 * Text user interface for the simulator: registers, memory, stack and queue.
 * All methods only read state (peek), so showing the UI never changes the
 * memory access statistics.
 */
public class ConsoleView {

    public static void showRegisters(CPU cpu) {
        cpu.displayState();
    }

    public static void showMemory(CPU cpu, int start, int end, String title) {
        System.out.println("---------- MEMORY: " + title + String.format(" (%02XH-%02XH)", start, end) + " ----------");
        System.out.print(cpu.getMemory().dump(start, end));
        Memory m = cpu.getMemory();
        System.out.println(String.format("bus reads=%d  writes=%d  last read=%s  last write=%s",
                m.getReadCount(), m.getWriteCount(),
                addr(m.getLastReadAddress()), addr(m.getLastWriteAddress())));
    }

    private static String addr(int a) {
        return a < 0 ? "-" : String.format("%02XH", a);
    }

    public static void showStack(CPU cpu) {
        int sp = cpu.getSP();
        System.out.println(String.format("---------- STACK  SP=%02XH  depth=%d ----------", sp, cpu.getStackDepth()));
        if (cpu.getStackDepth() == 0) {
            System.out.println("(empty)");
            return;
        }
        for (int a = sp; a > CPU.STACK_BASE; a--) {
            System.out.println(String.format("  %02XH | %02XH %s", a, cpu.getMemory().peek(a),
                    a == sp ? "<- SP (top)" : ""));
        }
    }

    public static void showQueue(Memory memory) {
        FifoQueue q = FifoQueue.attach(memory);
        System.out.println(String.format(
                "---------- QUEUE  count=%d/%d  head=%d  tail=%d  status=%s%s%s ----------",
                q.size(), FifoQueue.CAPACITY, q.getHead(), q.getTail(),
                FifoQueue.statusName(q.getStatus()),
                q.isEmpty() ? "  [EMPTY]" : "", q.isFull() ? "  [FULL]" : ""));
        for (int i = 0; i < FifoQueue.CAPACITY; i++) {
            boolean live = ((i - q.getHead() + FifoQueue.CAPACITY) % FifoQueue.CAPACITY) < q.size();
            String marks = (i == q.getHead() ? " <- HEAD (next dequeue)" : "")
                         + (i == q.getTail() ? " <- TAIL (next enqueue)" : "");
            System.out.println(String.format("  slot %d @%02XH : %02XH  %s%s", i,
                    FifoQueue.BUFFER_BASE + i, memory.peek(FifoQueue.BUFFER_BASE + i),
                    live ? "[in queue]" : "[free/stale]", marks));
        }
        System.out.println("  FIFO order (oldest -> newest): " + q.toList());
    }
}
