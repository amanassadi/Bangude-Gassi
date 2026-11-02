import java.util.ArrayList;
import java.util.List;

/**
 * FIFO queue stored in the simulator's memory as a circular buffer.
 *
 * The same memory layout is used by the assembly routines ENQ/DEQ in
 * programs/queue_test.asm, so the Java object and the assembly program can
 * be compared byte for byte.
 *
 *   20H HEAD    index of the oldest element (next to dequeue)
 *   21H TAIL    index where the next element will be stored
 *   22H COUNT   number of elements currently stored (0..8)
 *   23H STATUS  result of the last operation: 0 OK, 1 FULL, 2 EMPTY
 *   30H-37H     data buffer (8 slots)
 */
public class FifoQueue {
    public static final int HEAD_ADDR   = 0x20;
    public static final int TAIL_ADDR   = 0x21;
    public static final int COUNT_ADDR  = 0x22;
    public static final int STATUS_ADDR = 0x23;
    public static final int BUFFER_BASE = 0x30;
    public static final int CAPACITY    = 8;

    public static final int STATUS_OK    = 0;
    public static final int STATUS_FULL  = 1;
    public static final int STATUS_EMPTY = 2;

    private final Memory memory;

    /** Create a queue in this memory and initialise it (empty). */
    public FifoQueue(Memory memory) {
        this(memory, true);
    }

    private FifoQueue(Memory memory, boolean initialise) {
        this.memory = memory;
        if (initialise) init();
    }

    /**
     * Attach to a queue that already exists in memory (for example one built
     * by the assembly program) WITHOUT modifying it. Used by the user interface
     * and by the validation code.
     */
    public static FifoQueue attach(Memory memory) {
        return new FifoQueue(memory, false);
    }

    /** Reset head, tail, count and status to 0 (buffer contents untouched). */
    public final void init() {
        memory.write(HEAD_ADDR, 0);
        memory.write(TAIL_ADDR, 0);
        memory.write(COUNT_ADDR, 0);
        memory.write(STATUS_ADDR, STATUS_OK);
    }

    /** Enqueue at the tail. Returns false (and sets STATUS=FULL) when full. */
    public boolean enqueue(int value) {
        if (isFull()) {
            memory.write(STATUS_ADDR, STATUS_FULL);
            return false;
        }
        int tail = memory.read(TAIL_ADDR);
        memory.write(BUFFER_BASE + tail, value);
        memory.write(TAIL_ADDR, (tail + 1) % CAPACITY);
        memory.write(COUNT_ADDR, size() + 1);
        memory.write(STATUS_ADDR, STATUS_OK);
        return true;
    }

    /** Dequeue from the head. Returns -1 (and sets STATUS=EMPTY) when empty. */
    public int dequeue() {
        if (isEmpty()) {
            memory.write(STATUS_ADDR, STATUS_EMPTY);
            return -1;
        }
        int head = memory.read(HEAD_ADDR);
        int value = memory.read(BUFFER_BASE + head);
        memory.write(HEAD_ADDR, (head + 1) % CAPACITY);
        memory.write(COUNT_ADDR, size() - 1);
        memory.write(STATUS_ADDR, STATUS_OK);
        return value;
    }

    public int size()        { return memory.peek(COUNT_ADDR); }
    public boolean isEmpty() { return size() == 0; }
    public boolean isFull()  { return size() == CAPACITY; }
    public int getHead()     { return memory.peek(HEAD_ADDR); }
    public int getTail()     { return memory.peek(TAIL_ADDR); }
    public int getStatus()   { return memory.peek(STATUS_ADDR); }

    /** Live elements from oldest to newest. */
    public List<Integer> toList() {
        List<Integer> out = new ArrayList<>();
        int head = getHead();
        for (int i = 0; i < size(); i++) {
            out.add(memory.peek(BUFFER_BASE + (head + i) % CAPACITY));
        }
        return out;
    }

    public static String statusName(int status) {
        switch (status) {
            case STATUS_OK:    return "OK";
            case STATUS_FULL:  return "FULL";
            case STATUS_EMPTY: return "EMPTY";
            default:           return "UNKNOWN(" + status + ")";
        }
    }

    @Override
    public String toString() {
        return String.format("Queue[count=%d/%d head=%d tail=%d status=%s %s]",
                size(), CAPACITY, getHead(), getTail(), statusName(getStatus()), toList());
    }
}
