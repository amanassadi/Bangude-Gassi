import java.util.ArrayList;

public class ProgramLoader {
    public ArrayList<Instruction> loadFromAssembly(String[] code) {
        return new ArrayList<>(Assembler.assemble(String.join("\n", code)));
    }
}
