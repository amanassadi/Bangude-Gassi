public class Stack {
    private final int[] stack = new int[256];
    private int SP = 7;

    public void push(int value) {
        if (SP >= 255) throw new IllegalStateException("Stack Overflow");
        SP++;
        stack[SP] = value & 0xFF;
    }

    public int pop() {
        if (SP <= 7) throw new IllegalStateException("Stack Underflow");
        int value = stack[SP];
        stack[SP] = 0;
        SP--;
        return value;
    }

    public int peek() {
        if (SP <= 7) throw new IllegalStateException("Stack is empty");
        return stack[SP];
    }

    public int getSP() { return SP; }

    public boolean isEmpty() { return SP == 7; }

    public void display() {
        System.out.println("===== STACK =====");
        System.out.println("SP : " + SP);
        if (isEmpty()) {
            System.out.println("Stack is empty");
            return;
        }
        for (int i = SP; i > 7; i--) {
            System.out.println("Address " + i + " : " + stack[i]);
        }
    }
}
