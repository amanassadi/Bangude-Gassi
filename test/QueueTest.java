import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class QueueTest {
    public static void main(String[] args) {
        System.out.println("===== FIFO QUEUE TEST CASES =====");

        Memory mem = new Memory();
        FifoQueue q = new FifoQueue(mem);
        TestSupport.check("Q1  new queue is empty", q.isEmpty() && !q.isFull() && q.size() == 0);
        TestSupport.check("Q2  dequeue on empty returns -1, status EMPTY",
                q.dequeue() == -1 && q.getStatus() == FifoQueue.STATUS_EMPTY);

        q.enqueue(10); q.enqueue(20); q.enqueue(30);
        TestSupport.check("Q3  3 enqueues -> size 3, status OK", q.size() == 3 && q.getStatus() == FifoQueue.STATUS_OK);
        TestSupport.check("Q4  FIFO order preserved in toList()", q.toList().toString().equals("[10, 20, 30]"));
        TestSupport.check("Q5  dequeue returns oldest first (10,20,30)",
                q.dequeue() == 10 && q.dequeue() == 20 && q.dequeue() == 30);
        TestSupport.check("Q6  empty again after removing all", q.isEmpty());

        q = new FifoQueue(new Memory());
        for (int i = 1; i <= 8; i++) q.enqueue(i);
        TestSupport.check("Q7  8 enqueues fill the queue", q.isFull() && q.size() == 8);
        TestSupport.check("Q8  enqueue on full is rejected, status FULL",
                !q.enqueue(99) && q.getStatus() == FifoQueue.STATUS_FULL);
        TestSupport.check("Q9  rejected value not stored; queue unchanged",
                q.size() == 8 && q.toList().toString().equals("[1, 2, 3, 4, 5, 6, 7, 8]"));

        q.dequeue(); q.dequeue(); q.dequeue();
        q.enqueue(9); q.enqueue(10);
        TestSupport.check("Q10 wrap-around: tail wraps to index 2", q.getTail() == 2);
        TestSupport.check("Q11 order kept across wrap-around", q.toList().toString().equals("[4, 5, 6, 7, 8, 9, 10]"));

        q = new FifoQueue(new Memory());
        q.enqueue(0x55);
        TestSupport.check("Q12 value stored in buffer slot 0 (30H)", q.size() == 1);
        q = new FifoQueue(mem = new Memory());
        q.enqueue(0x55);
        TestSupport.check("Q13 memory layout: BUF[0]=55H HEAD=0 TAIL=1 COUNT=1",
                mem.peek(0x30) == 0x55 && mem.peek(0x20) == 0 && mem.peek(0x21) == 1 && mem.peek(0x22) == 1);

        q = new FifoQueue(new Memory());
        q.enqueue(1); q.dequeue(); q.enqueue(2);
        TestSupport.check("Q14 interleaved enqueue/dequeue", q.dequeue() == 2 && q.isEmpty());

        // randomised comparison with java.util.ArrayDeque
        Random rnd = new Random(12345);
        Deque<Integer> ref = new ArrayDeque<>();
        q = new FifoQueue(new Memory());
        boolean same = true;
        for (int i = 0; i < 5000 && same; i++) {
            if (rnd.nextBoolean()) {
                int v = rnd.nextInt(256);
                boolean ok = q.enqueue(v);
                if (ref.size() < FifoQueue.CAPACITY) { ref.addLast(v); same = ok; } else same = !ok;
            } else {
                int got = q.dequeue();
                Integer exp = ref.pollFirst();
                same = (exp == null) ? got == -1 : got == exp;
            }
            same = same && q.size() == ref.size();
        }
        TestSupport.check("Q15 5000 random operations match java.util.ArrayDeque", same);

        TestSupport.check("Q16 attach() does not modify an existing queue", attachKeepsData());

        TestSupport.summary("QUEUE TEST");
    }

    private static boolean attachKeepsData() {
        Memory m = new Memory();
        FifoQueue a = new FifoQueue(m);
        a.enqueue(7);
        FifoQueue b = FifoQueue.attach(m);
        return b.size() == 1 && b.dequeue() == 7;
    }
}
