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
        System.out.println("===== STC89C52 WEEK 3 DEMO =====");
        cpu.run();
        cpu.displayState();

        System.out.println("\n===== MEMORY =====");
        cpu.getMemory().write(30, 100);
        cpu.getMemory().write(31, 200);
        cpu.getMemory().display();

        System.out.println("\n===== STACK =====");
        cpu.getStack().push(10);
        cpu.getStack().push(20);
        cpu.getStack().push(30);
        cpu.getStack().display();
        System.out.println("POP: " + cpu.getStack().pop());
        cpu.getStack().display();

        System.out.println("\n===== FIFO QUEUE =====");
        cpu.getQueue().enqueue(10);
        cpu.getQueue().enqueue(20);
        cpu.getQueue().enqueue(30);
        cpu.getQueue().display();
        System.out.println("DEQUEUE: " + cpu.getQueue().dequeue());
        System.out.println("DEQUEUE: " + cpu.getQueue().dequeue());
        cpu.getQueue().display();
    }
}
