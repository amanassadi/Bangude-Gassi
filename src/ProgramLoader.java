import java.util.*;
public class ProgramLoader {
    private HashMap<String, Integer> labelMap = new HashMap<>();
    public ArrayList<Instruction> loadFromAssembly(String[] code){
        ArrayList<Instruction> prog = new ArrayList<>();
        int addr=0;
        for(String line: code){
            line=line.split("#")[0].trim(); if(line.isEmpty()) continue;
            if(line.contains(":")){ labelMap.put(line.split(":")[0].trim(), addr); if(line.split(":").length>1 &&!line.split(":")[1].trim().isEmpty()) addr++; }
            else addr++;
        }
        for(String line: code){
            line=line.split("#")[0].trim(); if(line.isEmpty()||line.endsWith(":")) continue;
            if(line.contains(":")) line=line.split(":",2)[1].trim(); if(line.isEmpty()) continue;
            String[] p=line.replace(","," ").trim().split("\\s+");
            switch(p[0].toUpperCase()){
                case "LOAD": prog.add(new Instruction(Instruction.Opcode.LOAD, p[1], Integer.parseInt(p[2]))); break;
                case "STORE": prog.add(new Instruction(Instruction.Opcode.STORE, p[1], Integer.parseInt(p[2]))); break;
                case "ADD": prog.add(new Instruction(Instruction.Opcode.ADD, p[1], p[2], p[3])); break;
                case "SUB": prog.add(new Instruction(Instruction.Opcode.SUB, p[1], p[2], p[3])); break;
                case "PUSH": prog.add(new Instruction(Instruction.Opcode.PUSH, p[1])); break;
                case "POP": prog.add(new Instruction(Instruction.Opcode.POP, p[1])); break;
                case "JMP": int a=labelMap.containsKey(p[1])?labelMap.get(p[1]):Integer.parseInt(p[1]); prog.add(new Instruction(Instruction.Opcode.JMP, "R0", a)); break;
                case "HLT": prog.add(new Instruction(Instruction.Opcode.HLT)); break;
            }
        }
        return prog;
    }
}