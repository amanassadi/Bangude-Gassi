/**
 * One assembly instruction in decoded form.
 *
 *   opcode      mnemonic, e.g. MOV
 *   destination first operand (or the only operand for INC, PUSH, ...)
 *   operand     second operand (or the branch target for SJMP/JZ/DJNZ/ACALL ...)
 *   target      third operand, used only by CJNE (branch target)
 *
 * Week 3: added the 'target' field (needed by CJNE) and fixed toString()
 * so that instructions such as "SJMP 0" print their operand.
 */
public class Instruction {
    private final String opcode;
    private final String destination;
    private final String operand;
    private final String target;

    public Instruction(String opcode, String destination, String operand, String target) {
        this.opcode = opcode.toUpperCase();
        this.destination = destination == null ? "" : destination.toUpperCase();
        this.operand = operand == null ? "" : operand.toUpperCase();
        this.target = target == null ? "" : target.toUpperCase();
    }

    public Instruction(String opcode, String destination, String operand) {
        this(opcode, destination, operand, "");
    }

    public Instruction(String opcode) {
        this(opcode, "", "", "");
    }

    public String getOpcode()      { return opcode; }
    public String getDestination() { return destination; }
    public String getOperand()     { return operand; }
    public String getTarget()      { return target; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(opcode);
        String sep = " ";
        for (String part : new String[]{destination, operand, target}) {
            if (!part.isEmpty()) {
                sb.append(sep).append(part);
                sep = ",";
            }
        }
        return sb.toString();
    }
}
