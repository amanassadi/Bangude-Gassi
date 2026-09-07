package simulator;

public class DataMemory {

    private int[] memory = new int[256];

    // Check whether the address is valid
    private void checkAddress(int address) {
        if (address < 0 || address > 0xFF) {
            throw new IllegalArgumentException("Invalid memory address");
        }
    }

    // Write value to memory
    public void write(int address, int value) {
        checkAddress(address);
        memory[address] = value & 0xFF;
    }

    // Read value from memory
    public int read(int address) {
        checkAddress(address);
        return memory[address];
    }

    // Clear all memory
    public void reset() {
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0;
        }
    }
}
