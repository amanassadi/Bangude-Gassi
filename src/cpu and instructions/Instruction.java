public class Instruction {
    private final String opcode;
    private final String destination;
    private final String operand;

    public Instruction(String opcode, String destination, String operand) {
        this.opcode = opcode.toUpperCase();
        this.destination = destination == null ? "" : destination.toUpperCase();
        this.operand = operand == null ? "" : operand.toUpperCase();
    }

    public Instruction(String opcode) {
        this(opcode, "", "");
    }

    public String getOpcode() {
        return opcode;
    }

    public String getDestination() {
        return destination;
    }

    public String getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        if (destination.isEmpty()) return opcode;
        if (operand.isEmpty()) return opcode + " " + destination;
        return opcode + " " + destination + "," + operand;
    }
}
