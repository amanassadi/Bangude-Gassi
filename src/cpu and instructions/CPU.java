import java.util.ArrayList;
import java.util.List;

public class CPU {
    // 8-bit registers
    private int A, B;
    private int R0, R1, R2, R3, R4, R5, R6, R7;

    // Special registers
    private int PC;
    private int SP;
    private int DPTR; // 16-bit

    // Flags
    private boolean CY;
    private boolean AC;
    private boolean OV;
    private boolean P;

    private final List<Instruction> program = new ArrayList<>();
    private boolean halted;

    public CPU() {
        reset();
    }

    public void reset() {
        A = B = 0;
        R0 = R1 = R2 = R3 = R4 = R5 = R6 = R7 = 0;
        PC = 0;
        SP = 7;
        DPTR = 0;
        CY = AC = OV = P = false;
        halted = false;
    }

    public void loadProgram(List<Instruction> instructions) {
        program.clear();
        program.addAll(instructions);
        reset();
    }

    public Instruction fetch() {
        if (halted || PC < 0 || PC >= program.size()) {
            return null;
        }
        return program.get(PC);
    }

    public void decode(Instruction instruction) {
        if (instruction == null) return;
        System.out.println("DECODE: " + instruction);
    }

    public void execute(Instruction instruction) {
        if (instruction == null || halted) return;

        String opcode = instruction.getOpcode();
        String destination = instruction.getDestination();
        String operand = instruction.getOperand();

        switch (opcode) {
            case "MOV":
                requireDestination(destination);
                setRegister(destination, resolveOperand(operand));
                PC++;
                break;

            case "ADD":
                requireDestination(destination);
                requireDestinationIsA(destination);
                int addValue = resolveOperand(operand);
                int addResult = A + addValue;
                CY = addResult > 0xFF;
                AC = ((A & 0x0F) + (addValue & 0x0F)) > 0x0F;
                OV = ((~(A ^ addValue) & (A ^ addResult)) & 0x80) != 0;
                A = addResult & 0xFF;
                updateParity();
                PC++;
                break;

            case "SUBB":
                requireDestination(destination);
                requireDestinationIsA(destination);
                int subValue = resolveOperand(operand);
                int borrow = CY ? 1 : 0;
                int oldA = A;
                int subResult = oldA - subValue - borrow;
                CY = subResult < 0;
                AC = ((oldA & 0x0F) - (subValue & 0x0F) - borrow) < 0;
                OV = (((oldA ^ subValue) & (oldA ^ subResult)) & 0x80) != 0;
                A = subResult & 0xFF;
                updateParity();
                PC++;
                break;

            case "INC":
                requireDestination(destination);
                setRegister(destination, getRegister(destination) + 1);
                updateParityIfA(destination);
                PC++;
                break;

            case "DEC":
                requireDestination(destination);
                setRegister(destination, getRegister(destination) - 1);
                updateParityIfA(destination);
                PC++;
                break;

            case "ANL":
                requireDestinationIsA(destination);
                A = A & resolveOperand(operand);
                updateParity();
                PC++;
                break;

            case "ORL":
                requireDestinationIsA(destination);
                A = A | resolveOperand(operand);
                updateParity();
                PC++;
                break;

            case "SJMP":
                // Educational simulator: operand is the absolute program index.
                PC = parseNumber(operand);
                break;

            default:
                throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }
    }

    public void step() {
        if (halted) return;

        Instruction instruction = fetch();
        if (instruction == null) {
            halted = true;
            return;
        }

        System.out.println("PC=" + PC + " | FETCH: " + instruction);
        decode(instruction);
        execute(instruction);
        System.out.println("EXECUTE COMPLETE | PC=" + PC + " | A=" + A);
    }

    public void run() {
        while (!halted && PC >= 0 && PC < program.size()) {
            step();
        }
    }

    private int resolveOperand(String operand) {
        if (operand == null || operand.isEmpty()) {
            throw new IllegalArgumentException("Missing operand");
        }

        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }

        return getRegister(operand);
    }

    private int parseNumber(String text) {
        String value = text.trim().toUpperCase();

        if (value.startsWith("0X")) {
            return Integer.parseInt(value.substring(2), 16) & 0xFF;
        }

        if (value.endsWith("H")) {
            return Integer.parseInt(value.substring(0, value.length() - 1), 16) & 0xFF;
        }

        return Integer.parseInt(value) & 0xFF;
    }

    private int getRegister(String register) {
        switch (register) {
            case "A": return A;
            case "B": return B;
            case "R0": return R0;
            case "R1": return R1;
            case "R2": return R2;
            case "R3": return R3;
            case "R4": return R4;
            case "R5": return R5;
            case "R6": return R6;
            case "R7": return R7;
            default:
                throw new IllegalArgumentException("Unknown register: " + register);
        }
    }

    public int getRegisterValue(String register) {
        return getRegister(register.toUpperCase());
    }

    private void setRegister(String register, int value) {
        value &= 0xFF;

        switch (register) {
            case "A": A = value; break;
            case "B": B = value; break;
            case "R0": R0 = value; break;
            case "R1": R1 = value; break;
            case "R2": R2 = value; break;
            case "R3": R3 = value; break;
            case "R4": R4 = value; break;
            case "R5": R5 = value; break;
            case "R6": R6 = value; break;
            case "R7": R7 = value; break;
            default:
                throw new IllegalArgumentException("Unknown register: " + register);
        }
    }

    private void requireDestination(String destination) {
        if (destination == null || destination.isEmpty()) {
            throw new IllegalArgumentException("Missing destination");
        }
    }

    private void requireDestinationIsA(String destination) {
        if (!"A".equals(destination)) {
            throw new IllegalArgumentException("This instruction requires A as destination");
        }
    }

    private void updateParityIfA(String destination) {
        if ("A".equals(destination)) updateParity();
    }

    private void updateParity() {
        P = Integer.bitCount(A & 0xFF) % 2 != 0;
    }

    public int getA() { return A; }
    public int getPC() { return PC; }
    public boolean getCY() { return CY; }
    public boolean getAC() { return AC; }
    public boolean getOV() { return OV; }
    public boolean getP() { return P; }
    public boolean isHalted() { return halted; }

    public void displayState() {
        System.out.println("========== CPU STATE ==========");
        System.out.println("A    : " + A);
        System.out.println("B    : " + B);
        System.out.println("R0   : " + R0);
        System.out.println("R1   : " + R1);
        System.out.println("R2   : " + R2);
        System.out.println("R3   : " + R3);
        System.out.println("R4   : " + R4);
        System.out.println("R5   : " + R5);
        System.out.println("R6   : " + R6);
        System.out.println("R7   : " + R7);
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
