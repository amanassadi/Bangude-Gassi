
public class Stack {

    private final int[] memory = new int[256];
    private int SP = 0x07;

    // PUSH: Store a byte on the stack
    public void push(int value) {

        if (SP >= 0xFF) {
            throw new IllegalStateException("Stack Overflow!");
        }

        SP++;
        memory[SP] = value & 0xFF;
    }

    // POP: Remove and return the top byte
    public int pop() {

        if (SP <= 0x07) {
            throw new IllegalStateException("Stack Underflow!");
        }

        int value = memory[SP];
        memory[SP] = 0;
        SP--;

        return value;
    }

    // Return current Stack Pointer
    public int getSP() {
        return SP;
    }

    // Read a stack memory location
    public int peek(int address) {

        if (address < 0 || address > 255) {
            throw new IllegalArgumentException(
                "Invalid memory address!"
            );
        }

        return memory[address];
    }

    // Display stack contents
    public void displayStack() {

        System.out.printf("SP = %02XH%n", SP);

        if (SP == 0x07) {
            System.out.println("Stack is empty.");
            return;
        }

        for (int i = SP; i > 0x07; i--) {
            System.out.printf(
                "Address: %02XH | Value: %02XH%n",
                i, memory[i]
            );
        }
    }

    public static void main(String[] args) {

        Stack stack = new Stack();

        System.out.println("PUSH OPERATIONS");
        stack.push(0x10);
        stack.push(0x20);
        stack.push(0x30);
        stack.displayStack();

        System.out.println("\nPOP OPERATIONS");
        System.out.printf("Popped: %02XH%n", stack.pop());
        System.out.printf("Popped: %02XH%n", stack.pop());

        stack.displayStack();
    }
}
