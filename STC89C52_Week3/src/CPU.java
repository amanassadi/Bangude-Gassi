import java.util.ArrayList;
import java.util.List;

/**
 * STC89C52 (8051-core) CPU simulator - Week 3.
 *
 * Week 2 features kept: A, B, R0-R7, PC, SP, DPTR, flags CY/AC/OV/P,
 * FETCH-DECODE-EXECUTE cycle, MOV ADD SUBB INC DEC ANL ORL SJMP.
 *
 * Week 3 additions (CPU + instruction role):
 *   - R0-R7 now live in Memory (bank 0, addresses 00H-07H) like real hardware
 *   - Addressing modes: register, immediate (#n), direct (nn / nnH),
 *     register-indirect (@R0, @R1)
 *   - Stack: PUSH, POP, ACALL/LCALL, RET using SP (overflow/underflow checked)
 *   - Branches: JZ, JNZ, JC, JNC, CJNE, DJNZ, LJMP
 *   - Logic / flags: XRL, CLR, SETB, CPL, NOP
 *   - HALT pseudo-instruction (simulator-only, stops the run loop)
 *   - Parity flag now follows A after every instruction (as on real hardware)
 */
public class CPU {
    public static final int STACK_BASE = 0x07;      // SP value after reset
    public static final long MAX_CYCLES = 1_000_000; // runaway-program guard

    // Accumulator and B register (SFRs E0H and F0H)
    private int A, B;

    // Special registers
    private int PC;     // program index (educational simulator)
    private int SP;     // 8-bit stack pointer
    private int DPTR;   // 16-bit

    // Flags
    private boolean CY, AC, OV, P;

    private final Memory memory = new Memory();
    private final List<Instruction> program = new ArrayList<>();
    private boolean halted;
    private boolean trace = true;
    private long cycles;
    private int nextPC;

    public CPU() {
        reset();
    }

    public void reset() {
        A = B = 0;
        PC = 0;
        SP = STACK_BASE;
        DPTR = 0;
        CY = AC = OV = P = false;
        halted = false;
        cycles = 0;
        memory.clear();
    }

    public void loadProgram(List<Instruction> instructions) {
        program.clear();
        program.addAll(instructions);
        reset();
    }

    // ------------------------------------------------------------------
    // FETCH - DECODE - EXECUTE
    // ------------------------------------------------------------------

    public Instruction fetch() {
        if (halted || PC < 0 || PC >= program.size()) {
            return null;
        }
        return program.get(PC);
    }

    public void decode(Instruction instruction) {
        if (instruction == null) return;
        if (trace) System.out.println("DECODE: " + instruction);
    }

    public void execute(Instruction instruction) {
        if (instruction == null || halted) return;

        String op = instruction.getOpcode();
        String d = instruction.getDestination();
        String s = instruction.getOperand();
        String t = instruction.getTarget();

        nextPC = PC + 1;

        switch (op) {
            case "NOP":
                break;

            case "HALT":
                halted = true;
                nextPC = PC;
                break;

            case "MOV":
                need(d, "destination");
                if ("DPTR".equals(d)) {
                    DPTR = parseNumber16(stripImmediate(s));
                } else {
                    write(d, read(s));
                }
                break;

            case "ADD": {
                requireA(d);
                int v = read(s);
                int r = A + v;
                CY = r > 0xFF;
                AC = ((A & 0x0F) + (v & 0x0F)) > 0x0F;
                OV = ((~(A ^ v) & (A ^ r)) & 0x80) != 0;
                A = r & 0xFF;
                break;
            }

            case "SUBB": {
                requireA(d);
                int v = read(s);
                int borrow = CY ? 1 : 0;
                int oldA = A;
                int r = oldA - v - borrow;
                CY = r < 0;
                AC = ((oldA & 0x0F) - (v & 0x0F) - borrow) < 0;
                OV = (((oldA ^ v) & (oldA ^ r)) & 0x80) != 0;
                A = r & 0xFF;
                break;
            }

            case "INC":
                need(d, "destination");
                if ("DPTR".equals(d)) DPTR = (DPTR + 1) & 0xFFFF;
                else write(d, read(d) + 1);
                break;

            case "DEC":
                need(d, "destination");
                write(d, read(d) - 1);
                break;

            case "ANL":
                need(d, "destination");
                write(d, read(d) & read(s));
                break;

            case "ORL":
                need(d, "destination");
                write(d, read(d) | read(s));
                break;

            case "XRL":
                need(d, "destination");
                write(d, read(d) ^ read(s));
                break;

            case "CLR":
                if ("A".equals(d) || "ACC".equals(d)) A = 0;
                else if ("C".equals(d)) CY = false;
                else throw new IllegalArgumentException("CLR supports only A or C, got: " + d);
                break;

            case "SETB":
                if ("C".equals(d)) CY = true;
                else throw new IllegalArgumentException("SETB supports only C, got: " + d);
                break;

            case "CPL":
                if ("A".equals(d) || "ACC".equals(d)) A = ~A & 0xFF;
                else if ("C".equals(d)) CY = !CY;
                else throw new IllegalArgumentException("CPL supports only A or C, got: " + d);
                break;

            // ---- branches ------------------------------------------------
            case "SJMP":
            case "LJMP":
                // Educational simulator: operand is the absolute program index.
                nextPC = parseNumber(s);
                break;

            case "JZ":
                if (A == 0) nextPC = parseNumber(s);
                break;

            case "JNZ":
                if (A != 0) nextPC = parseNumber(s);
                break;

            case "JC":
                if (CY) nextPC = parseNumber(s);
                break;

            case "JNC":
                if (!CY) nextPC = parseNumber(s);
                break;

            case "CJNE": {
                // CJNE left,right,target : jump if left != right, CY = (left < right)
                int left = read(d);
                int right = read(s);
                CY = left < right;
                if (left != right) nextPC = parseNumber(t);
                break;
            }

            case "DJNZ": {
                // DJNZ loc,target : loc = loc-1; jump if result != 0 (flags unchanged)
                int v = (read(d) - 1) & 0xFF;
                write(d, v);
                if (v != 0) nextPC = parseNumber(s);
                break;
            }

            // ---- stack / subroutines ------------------------------------
            case "PUSH":
                need(d, "operand");
                push(read(d));
                break;

            case "POP":
                need(d, "operand");
                write(d, pop());
                break;

            case "ACALL":
            case "LCALL": {
                int ret = PC + 1;
                push(ret & 0xFF);          // low byte first, like the real 8051
                push((ret >> 8) & 0xFF);   // then high byte
                nextPC = parseNumber(s);
                break;
            }

            case "RET": {
                int high = pop();
                int low = pop();
                nextPC = (high << 8) | low;
                break;
            }

            default:
                throw new IllegalArgumentException("Unknown opcode: " + op);
        }

        PC = nextPC;
        updateParity();   // P always reflects the parity of A
    }

    public void step() {
        if (halted) return;

        Instruction instruction = fetch();
        if (instruction == null) {
            halted = true;
            return;
        }

        if (trace) System.out.println("PC=" + PC + " | FETCH: " + instruction);
        decode(instruction);
        execute(instruction);
        cycles++;
        if (trace) System.out.println("EXECUTE COMPLETE | PC=" + PC + " | A=" + A + " | SP=" + SP);
    }

    public void run() {
        while (!halted) {
            if (PC < 0 || PC >= program.size()) {
                halted = true;           // ran off the end of the program
                break;
            }
            if (cycles >= MAX_CYCLES) {
                throw new IllegalStateException("Cycle limit reached - possible infinite loop");
            }
            step();
        }
    }

    // ------------------------------------------------------------------
    // Stack
    // ------------------------------------------------------------------

    /** PUSH: SP = SP + 1, then IRAM[SP] = value. */
    public void push(int value) {
        if (SP >= 0xFF) {
            throw new IllegalStateException("Stack overflow: SP=0xFF");
        }
        SP++;
        memory.write(SP, value);
    }

    /** POP: value = IRAM[SP], then SP = SP - 1. */
    public int pop() {
        if (SP <= STACK_BASE) {
            throw new IllegalStateException("Stack underflow: stack is empty (SP=0x"
                    + String.format("%02X", SP) + ")");
        }
        int value = memory.read(SP);
        SP--;
        return value;
    }

    /** Stack contents from bottom (08H) to top (SP). */
    public int[] getStackContents() {
        int n = SP - STACK_BASE;
        int[] out = new int[Math.max(n, 0)];
        for (int i = 0; i < out.length; i++) {
            out[i] = memory.peek(STACK_BASE + 1 + i);
        }
        return out;
    }

    public int getStackDepth() { return SP - STACK_BASE; }

    // ------------------------------------------------------------------
    // Operand access (addressing modes)
    // ------------------------------------------------------------------

    /** Read an operand: #imm | register | @R0/@R1 | direct address. */
    private int read(String operand) {
        if (operand == null || operand.isEmpty()) {
            throw new IllegalArgumentException("Missing operand");
        }
        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }
        if (operand.startsWith("@")) {
            return memory.read(indirectAddress(operand));
        }
        if (isRegisterName(operand)) {
            return getRegister(operand);
        }
        return readDirect(parseNumber(operand));
    }

    /** Write to a location: register | @R0/@R1 | direct address. */
    private void write(String location, int value) {
        value &= 0xFF;
        if (location.startsWith("#")) {
            throw new IllegalArgumentException("Cannot write to an immediate value: " + location);
        }
        if (location.startsWith("@")) {
            memory.write(indirectAddress(location), value);
        } else if (isRegisterName(location)) {
            setRegister(location, value);
        } else {
            writeDirect(parseNumber(location), value);
        }
    }

    private int indirectAddress(String operand) {
        String r = operand.substring(1);
        if (!r.equals("R0") && !r.equals("R1")) {
            throw new IllegalArgumentException("Indirect addressing only allowed with @R0 or @R1, got: " + operand);
        }
        return getRegister(r);
    }

    private int readDirect(int address) {
        if (address < 0x80) return memory.read(address);
        switch (address) {
            case 0xE0: return A;
            case 0xF0: return B;
            case 0x81: return SP;
            default:
                throw new IllegalArgumentException(
                    String.format("Direct address 0x%02X is an unmodelled SFR", address));
        }
    }

    private void writeDirect(int address, int value) {
        if (address < 0x80) { memory.write(address, value); return; }
        switch (address) {
            case 0xE0: A = value; break;
            case 0xF0: B = value; break;
            case 0x81: SP = value; break;
            default:
                throw new IllegalArgumentException(
                    String.format("Direct address 0x%02X is an unmodelled SFR", address));
        }
    }

    private boolean isRegisterName(String s) {
        switch (s) {
            case "A": case "ACC": case "B": case "SP":
            case "R0": case "R1": case "R2": case "R3":
            case "R4": case "R5": case "R6": case "R7":
                return true;
            default:
                return false;
        }
    }

    private int getRegister(String register) {
        switch (register) {
            case "A": case "ACC": return A;
            case "B": return B;
            case "SP": return SP;
            case "R0": case "R1": case "R2": case "R3":
            case "R4": case "R5": case "R6": case "R7":
                return memory.peek(register.charAt(1) - '0');   // bank 0
            default:
                throw new IllegalArgumentException("Unknown register: " + register);
        }
    }

    private void setRegister(String register, int value) {
        value &= 0xFF;
        switch (register) {
            case "A": case "ACC": A = value; break;
            case "B": B = value; break;
            case "SP": SP = value; break;
            case "R0": case "R1": case "R2": case "R3":
            case "R4": case "R5": case "R6": case "R7":
                memory.poke(register.charAt(1) - '0', value);
                break;
            default:
                throw new IllegalArgumentException("Unknown register: " + register);
        }
    }

    public int getRegisterValue(String register) {
        return getRegister(register.toUpperCase());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String stripImmediate(String s) {
        if (s == null || !s.startsWith("#")) {
            throw new IllegalArgumentException("Expected an immediate value (#n), got: " + s);
        }
        return s.substring(1);
    }

    private int parseNumber(String text) {
        return parseValue(text) & 0xFF;
    }

    private int parseNumber16(String text) {
        return parseValue(text) & 0xFFFF;
    }

    private int parseValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing numeric value");
        }
        String value = text.trim().toUpperCase();
        try {
            if (value.startsWith("0X")) return Integer.parseInt(value.substring(2), 16);
            if (value.endsWith("H"))    return Integer.parseInt(value.substring(0, value.length() - 1), 16);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad operand (not a register or number): " + text);
        }
    }

    private void need(String value, String what) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing " + what);
        }
    }

    private void requireA(String destination) {
        need(destination, "destination");
        if (!"A".equals(destination)) {
            throw new IllegalArgumentException("This instruction requires A as destination");
        }
    }

    private void updateParity() {
        P = Integer.bitCount(A & 0xFF) % 2 != 0;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public int getA()      { return A; }
    public int getB()      { return B; }
    public int getPC()     { return PC; }
    public int getSP()     { return SP; }
    public int getDPTR()   { return DPTR; }
    public boolean getCY() { return CY; }
    public boolean getAC() { return AC; }
    public boolean getOV() { return OV; }
    public boolean getP()  { return P; }
    public boolean isHalted() { return halted; }
    public long getCycles()   { return cycles; }
    public Memory getMemory() { return memory; }
    public int getProgramSize() { return program.size(); }
    public void setTrace(boolean trace) { this.trace = trace; }

    public void displayState() {
        System.out.println("========== CPU STATE ==========");
        System.out.println("A    : " + A);
        System.out.println("B    : " + B);
        for (int i = 0; i < 8; i++) {
            System.out.println("R" + i + "   : " + memory.peek(i));
        }
        System.out.println("PC   : " + PC);
        System.out.println("SP   : " + SP);
        System.out.println("DPTR : " + DPTR);
        System.out.println("CY   : " + CY);
        System.out.println("AC   : " + AC);
        System.out.println("OV   : " + OV);
        System.out.println("P    : " + P);
        System.out.println("===============================");
    }
}
