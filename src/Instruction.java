public class Instruction {
    public enum Opcode { LOAD, STORE, ADD, SUB, PUSH, POP, JMP, HLT }
    public Opcode opcode;
    public String rd, rs1, rs2;
    public int address;
    public String label;
    public Instruction(Opcode opcode, String reg, int address){ this.opcode=opcode; this.rd=reg; this.address=address; }
    public Instruction(Opcode opcode, String rd, String rs1, String rs2){ this.opcode=opcode; this.rd=rd; this.rs1=rs1; this.rs2=rs2; }
    public Instruction(Opcode opcode, String reg){ this.opcode=opcode; this.rd=reg; }
    public Instruction(Opcode opcode){ this.opcode=opcode; }
}