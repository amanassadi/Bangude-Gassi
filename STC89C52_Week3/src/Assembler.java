import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal two-pass assembler: turns 8051-style assembly text into the
 * Instruction objects the CPU executes.
 *
 * Syntax
 *   ; comment                      anything after ';' is ignored
 *   LABEL:  MOV A,#05H             optional label before an instruction
 *   NAME    EQU 30H                symbolic constant (usable as #NAME, NAME, @NAME is not allowed)
 *   Numbers: 10 (decimal), 1AH / 0FFH (hex, leading digit required), 0x1A
 *
 * Branch targets may be labels; they are resolved to program indexes.
 */
public class Assembler {

    private static final Set<String> JUMP_OPERAND = new HashSet<>(Arrays.asList(
            "SJMP", "LJMP", "JZ", "JNZ", "JC", "JNC", "ACALL", "LCALL"));

    private static final Map<String, Integer> ARITY = new HashMap<>();
    static {
        for (String s : new String[]{"NOP", "RET", "HALT"}) ARITY.put(s, 0);
        for (String s : new String[]{"INC", "DEC", "PUSH", "POP", "CLR", "SETB", "CPL",
                "SJMP", "LJMP", "JZ", "JNZ", "JC", "JNC", "ACALL", "LCALL"}) ARITY.put(s, 1);
        for (String s : new String[]{"MOV", "ADD", "SUBB", "ANL", "ORL", "XRL", "DJNZ"}) ARITY.put(s, 2);
        ARITY.put("CJNE", 3);
    }

    private static class Line {
        final int number;
        final String opcode;
        final List<String> args;
        Line(int number, String opcode, List<String> args) {
            this.number = number;
            this.opcode = opcode;
            this.args = args;
        }
    }

    public static List<Instruction> assembleFile(String path) throws IOException {
        return assemble(new String(Files.readAllBytes(Paths.get(path))));
    }

    public static List<Instruction> assemble(String source) {
        Map<String, Integer> labels = new HashMap<>();
        Map<String, String> equ = new HashMap<>();
        List<Line> lines = new ArrayList<>();

        // ---------------- pass 1: labels, EQU, split into tokens -----------
        String[] rawLines = source.split("\\r?\\n");
        for (int i = 0; i < rawLines.length; i++) {
            int lineNo = i + 1;
            String text = rawLines[i];
            int c = text.indexOf(';');
            if (c >= 0) text = text.substring(0, c);
            text = text.trim().toUpperCase();
            if (text.isEmpty()) continue;

            // NAME EQU value
            String[] words = text.split("\\s+");
            if (words.length >= 3 && words[1].equals("EQU")) {
                equ.put(words[0], text.substring(text.indexOf("EQU") + 3).trim());
                continue;
            }

            // LABEL:
            int colon = text.indexOf(':');
            if (colon >= 0) {
                String label = text.substring(0, colon).trim();
                if (label.isEmpty() || label.contains(" ")) {
                    throw error(lineNo, "Invalid label");
                }
                if (labels.containsKey(label)) {
                    throw error(lineNo, "Duplicate label: " + label);
                }
                labels.put(label, lines.size());
                text = text.substring(colon + 1).trim();
                if (text.isEmpty()) continue;
            }

            // MNEMONIC operands
            String opcode;
            List<String> args = new ArrayList<>();
            int sp = indexOfWhitespace(text);
            if (sp < 0) {
                opcode = text;
            } else {
                opcode = text.substring(0, sp);
                for (String a : text.substring(sp).split(",")) {
                    String t = a.trim();
                    if (t.isEmpty()) throw error(lineNo, "Empty operand");
                    args.add(t);
                }
            }
            lines.add(new Line(lineNo, opcode, args));
        }

        // ---------------- pass 2: build Instruction objects ----------------
        List<Instruction> out = new ArrayList<>();
        for (Line l : lines) {
            Integer arity = ARITY.get(l.opcode);
            if (arity == null) throw error(l.number, "Unknown mnemonic: " + l.opcode);
            if (l.args.size() != arity) {
                throw error(l.number, l.opcode + " expects " + arity + " operand(s), found " + l.args.size());
            }
            List<String> a = new ArrayList<>();
            for (String raw : l.args) a.add(substitute(raw, equ));

            Instruction ins;
            switch (arity) {
                case 0:
                    ins = new Instruction(l.opcode);
                    break;
                case 1:
                    if (JUMP_OPERAND.contains(l.opcode)) {
                        ins = new Instruction(l.opcode, "", target(a.get(0), labels, l.number));
                    } else {
                        ins = new Instruction(l.opcode, a.get(0), "");
                    }
                    break;
                case 2:
                    if (l.opcode.equals("DJNZ")) {
                        ins = new Instruction(l.opcode, a.get(0), target(a.get(1), labels, l.number));
                    } else {
                        ins = new Instruction(l.opcode, a.get(0), a.get(1));
                    }
                    break;
                default: // CJNE
                    ins = new Instruction(l.opcode, a.get(0), a.get(1), target(a.get(2), labels, l.number));
                    break;
            }
            out.add(ins);
        }
        return out;
    }

    private static String substitute(String token, Map<String, String> equ) {
        String prefix = "";
        String body = token;
        if (body.startsWith("#") || body.startsWith("@")) {
            prefix = body.substring(0, 1);
            body = body.substring(1);
        }
        String value = equ.get(body);
        return value != null ? prefix + value : token;
    }

    private static String target(String token, Map<String, Integer> labels, int lineNo) {
        Integer index = labels.get(token);
        if (index != null) return String.valueOf(index);
        if (Character.isLetter(token.charAt(0)) && !token.endsWith("H")
                && !token.startsWith("0X")) {
            throw error(lineNo, "Undefined label: " + token);
        }
        return token;   // numeric target
    }

    private static int indexOfWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) return i;
        }
        return -1;
    }

    private static IllegalArgumentException error(int line, String message) {
        return new IllegalArgumentException("Assembly error, line " + line + ": " + message);
    }
}
