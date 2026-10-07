public class Memory {
    private final int[] memory = new int[256];

    public void write(int address, int value) {
        checkAddress(address);
        memory[address] = value & 0xFF;
    }

    public int read(int address) {
        checkAddress(address);
        return memory[address];
    }

    private void checkAddress(int address) {
        if (address < 0 || address >= 256) {
            throw new IllegalArgumentException("Invalid memory address: " + address);
        }
    }

    public void display() {
        System.out.println("===== MEMORY =====");
        boolean empty = true;
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                System.out.println("Address " + i + " : " + memory[i]);
                empty = false;
            }
        }
        if (empty) System.out.println("Memory is empty");
    }
}
