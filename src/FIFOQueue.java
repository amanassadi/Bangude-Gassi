public class FIFOQueue {
    private final int[] queue;
    private int front = 0;
    private int rear = -1;
    private int size = 0;

    public FIFOQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        queue = new int[capacity];
    }

    public void enqueue(int value) {
        if (isFull()) throw new IllegalStateException("Queue Full");
        rear = (rear + 1) % queue.length;
        queue[rear] = value & 0xFF;
        size++;
    }

    public int dequeue() {
        if (isEmpty()) throw new IllegalStateException("Queue Empty");
        int value = queue[front];
        queue[front] = 0;
        front = (front + 1) % queue.length;
        size--;
        return value;
    }

    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == queue.length; }
    public int size() { return size; }
    public int capacity() { return queue.length; }

    public void display() {
        System.out.println("===== FIFO QUEUE =====");
        if (isEmpty()) {
            System.out.println("Queue is empty");
            return;
        }
        for (int i = 0; i < size; i++) {
            int index = (front + i) % queue.length;
            System.out.print(queue[index] + " ");
        }
        System.out.println();
    }
}
