import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Week 4 IPC test: verifies Core <-> Logger and UI/Core protocol independently. */
public class IPCIntegrationTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== WEEK 4 IPC TEST ===");
        try(Socket s=new Socket("127.0.0.1",5000); BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream())); BufferedWriter out=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            send(out,"PING"); require(read(in).contains("PONG|CORE"),"Core PING/PONG");
            String source="MOV A,#05\nINC A\nHALT\n";
            send(out,"LOADSRC|"+Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8))); readBlock(in); 
            send(out,"RUN"); List<String> block=readBlock(in);
            require(contains(block,"STATE|PC=2|SP=7|A=6|B=0|CYCLES=3"),"Core executes Week 3 CPU program");
            require(contains(block,"STATUS|HALTED"),"Core reports HALTED");
        }
        Thread.sleep(300);
        require(Files.exists(Paths.get("execution.log")),"Logger created execution.log");
        String log=Files.readString(Paths.get("execution.log"));
        require(log.contains("Execution completed"),"Logger received Core execution log");
        System.out.println("PASS: UI/Core socket protocol");
        System.out.println("PASS: Core CPU execution");
        System.out.println("PASS: Core -> Logger socket logging");
        System.out.println("ALL WEEK 4 IPC TESTS PASSED");
    }
    private static void send(BufferedWriter o,String s)throws IOException{o.write(s);o.newLine();o.flush();}
    private static List<String> readBlock(BufferedReader in)throws IOException{List<String> x=new ArrayList<>();String s;while((s=in.readLine())!=null){if(s.equals("END"))break;x.add(s);}return x;}
    private static String read(BufferedReader in)throws IOException{return readBlock(in).stream().findFirst().orElse("");}
    private static boolean contains(List<String> l,String x){return l.stream().anyMatch(s->s.equals(x));}
    private static void require(boolean ok,String name){if(!ok)throw new AssertionError("FAIL: "+name);}
}
