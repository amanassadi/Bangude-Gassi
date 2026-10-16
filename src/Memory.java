/**
 * Internal data memory (IRAM) of the STC89C52: 256 bytes, addresses 00H-FFH.
 *
 * Layout used by the simulator (same as a real 8051):
 *   00H-07H  register bank 0 (R0-R7)
 *   08H-FFH  general RAM; the stack grows upward from 08H
 *            (SP resets to 07H, PUSH = increment then write)
 *
 * read()/write() are the "bus" accesses and are counted for statistics.
 * peek()/poke() are silent accesses used by the CPU for register-bank
 * cells and by the user-interface code, so they do not disturb the counters.
 */
public class Memory {
    public static final int SIZE = 256;

    private final int[] cells = new int[SIZE];
    private int readCount;
    private int writeCount;
    private int lastReadAddress = -1;
    private int lastWriteAddress = -1;

    private static void checkAddress(int address) {
        if (address < 0 || address >= SIZE) {
            throw new IllegalArgumentException(
                String.format("Memory address out of range: 0x%X (valid 0x00-0xFF)", address));
        }
    }

    /** Bus read: returns an 8-bit value. */
    public int read(int address) {
        checkAddress(address);
        readCount++;
        lastReadAddress = address;
        return cells[address];
    }

    /** Bus write: value is truncated to 8 bits. */
    public void write(int address, int value) {
        checkAddress(address);
        writeCount++;
        lastWriteAddress = address;
        cells[address] = value & 0xFF;
    }

    /** Silent read (no statistics). */
    public int peek(int address) {
        checkAddress(address);
        return cells[address];
    }

    /** Silent write (no statistics). */
    public void poke(int address, int value) {
        checkAddress(address);
        cells[address] = value & 0xFF;
    }

    public void clear() {
        java.util.Arrays.fill(cells, 0);
        resetStatistics();
    }

    public void resetStatistics() {
        readCount = writeCount = 0;
        lastReadAddress = lastWriteAddress = -1;
    }

    public int getReadCount()        { return readCount; }
    public int getWriteCount()       { return writeCount; }
    public int getLastReadAddress()  { return lastReadAddress; }
    public int getLastWriteAddress() { return lastWriteAddress; }

    /** Hex dump of [start..end], 8 bytes per row. */
    public String dump(int start, int end) {
        checkAddress(start);
        checkAddress(end);
        StringBuilder sb = new StringBuilder();
        for (int row = start & ~7; row <= end; row += 8) {
            sb.append(String.format("%02X: ", row));
            for (int i = 0; i < 8; i++) {
                int a = row + i;
                if (a < start || a > end) sb.append("   ");
                else sb.append(String.format("%02X ", cells[a]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
