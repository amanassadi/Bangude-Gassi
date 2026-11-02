import java.util.List;

public class QueueAsmTest {
    public static void main(String[] args) throws Exception {
        System.out.println("===== ASSEMBLY QUEUE VALIDATION =====");
        List<Instruction> program = Assembler.assembleFile("programs/queue_test.asm");
        CPU cpu = new CPU();
        cpu.loadProgram(program);
        cpu.setTrace(false);
        cpu.run();

        TestSupport.check("A1  program assembled (82 instructions)", program.size() == 82);
        TestSupport.check("A2  program halted normally", cpu.isHalted());

        QueueValidator v = new QueueValidator();
        v.validate(cpu);
        System.out.print(v.report());
        TestSupport.check("A3  all expected-vs-actual checks pass", v.getFailed() == 0);
        TestSupport.summary("QUEUE ASSEMBLY VALIDATION");
    }
}
