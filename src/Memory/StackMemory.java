package simulator;

public class StackMemory {

    private DataMemory memory;

    // 8051 stack starts at 07H
    private int sp = 0x07;

    public StackMemory(DataMemory memory) {
        this.memory = memory;
    }

    // Push value onto stack
    public void push(int value) {

        if (sp >= 0xFF) {
            throw new IllegalStateException("Stack overflow");
        }

        sp++;

        memory.write(sp, value);
    }

    // Pop value from stack
    public int pop() {

        if (sp <= 0x07) {
            throw new IllegalStateException("Stack underflow");
        }

        int value = memory.read(sp);

        sp--;

        return value;
    }

    // Get current stack pointer
    public int getSP() {
        return sp;
    }

    // Reset stack pointer
    public void reset() {
        sp = 0x07;
    }
}
