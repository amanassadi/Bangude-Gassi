import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        CPU cpu = new CPU();

        List<Instruction> program = new ArrayList<>();
        program.add(new Instruction("MOV", "R0", "#10"));
        program.add(new Instruction("MOV", "A", "#20"));
        program.add(new Instruction("ADD", "A", "R0"));
        program.add(new Instruction("INC", "R0", ""));
        program.add(new Instruction("DEC", "A", ""));
        program.add(new Instruction("ANL", "A", "#15"));
        program.add(new Instruction("ORL", "A", "#2"));

        cpu.loadProgram(program);

        System.out.println("===== STC89C52 WEEK 2 DEMO =====");
        cpu.run();
        cpu.displayState();
    }
}
