import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Week 4 Student 2: independent Core process containing the existing Week 3 engine. */
public class CoreProcess {
    public static final int PORT = 5000;
    private static CPU cpu = new CPU();
    private static List<Instruction> program = new ArrayList<>();
    private static LoggerClient logger;
    private static final ExecutorService clients = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        logger = new LoggerClient();
        try { logger.connect(); } catch(IOException e){ System.err.println("Logger unavailable; continuing without remote log."); }
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Core Process listening on port " + PORT);
            while (true) clients.submit(new ClientTask(server.accept()));
        } finally { clients.shutdownNow(); if(logger!=null)logger.close(); }
    }

    private static class ClientTask implements Runnable {
        private final Socket socket;
        ClientTask(Socket socket){this.socket=socket;}
        public void run(){
            try(Socket s=socket; BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream())); BufferedWriter out=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
                String line;
                while((line=in.readLine())!=null){
                    List<String> response;
                    try { response=process(line); }
                    catch(Exception e){ response=List.of("ERROR|"+safe(e.getMessage())); if(logger!=null)logger.log("ERROR", safe(e.getMessage())); }
                    for(String r:response){out.write(r);out.newLine();} out.write("END");out.newLine();out.flush();
                    if("QUIT".equals(line)) break;
                }
            } catch(IOException e){System.err.println("Core client error: "+e.getMessage());}
        }
    }

    private static synchronized List<String> process(String command) throws Exception {
        if(command.equals("PING")){ return List.of("PONG|CORE"); }
        if(command.equals("RESET")){cpu=new CPU(); program=new ArrayList<>(); log("INFO","CPU reset"); return state();}
        if(command.startsWith("LOADSRC|")){
            String source=new String(Base64.getDecoder().decode(command.substring(8)), StandardCharsets.UTF_8);
            program=Assembler.assemble(source); cpu=new CPU(); cpu.loadProgram(program); cpu.setTrace(false);
            log("INFO","Program loaded: "+program.size()+" instructions"); return state();
        }
        if(command.equals("STEP")){
            Instruction ins=cpu.fetch(); if(ins==null){cpu.run();log("INFO","Program ended");return state();}
            String text=ins.toString(); cpu.step(); log("INFO","STEP | "+text+" | A="+cpu.getA()+" PC="+cpu.getPC()); return state();
        }
        if(command.equals("RUN")){
            long start=System.nanoTime(); cpu.run(); long elapsed=System.nanoTime()-start;
            log("INFO","Execution completed | A="+cpu.getA()+" PC="+cpu.getPC()+" SP="+cpu.getSP()+" cycles="+cpu.getCycles()+" timeNs="+elapsed);
            return state();
        }
        if(command.equals("QUIT")){log("INFO","Core client disconnected");return List.of("BYE");}
        return List.of("ERROR|Unknown command");
    }

    private static List<String> state(){
        StringBuilder mem=new StringBuilder();
        for(int i=0;i<Memory.SIZE;i++){if(i>0)mem.append(',');mem.append(i).append(':').append(cpu.getMemory().peek(i));}
        int[] st=cpu.getStackContents(); StringBuilder stack=new StringBuilder();for(int i=0;i<st.length;i++){if(i>0)stack.append(',');stack.append(st[i]);}
        FifoQueue q=FifoQueue.attach(cpu.getMemory()); StringBuilder queue=new StringBuilder().append(q.getStatus());for(int v:q.toList())queue.append(';').append(v);
        return Arrays.asList(
            "STATUS|"+(cpu.isHalted()?"HALTED":"READY"),
            "STATE|PC="+cpu.getPC()+"|SP="+cpu.getSP()+"|A="+cpu.getA()+"|B="+cpu.getB()+"|CYCLES="+cpu.getCycles(),
            "REGS|R0="+cpu.getRegisterValue("R0")+"|R1="+cpu.getRegisterValue("R1")+"|R2="+cpu.getRegisterValue("R2")+"|R3="+cpu.getRegisterValue("R3")+"|R4="+cpu.getRegisterValue("R4")+"|R5="+cpu.getRegisterValue("R5")+"|R6="+cpu.getRegisterValue("R6")+"|R7="+cpu.getRegisterValue("R7"),
            "FLAGS|CY="+(cpu.getCY()?1:0)+"|AC="+(cpu.getAC()?1:0)+"|OV="+(cpu.getOV()?1:0)+"|P="+(cpu.getP()?1:0),
            "MEM|"+mem,
            "STACK|"+stack,
            "QUEUE|"+queue
        );
    }
    private static void log(String level,String msg){if(logger!=null)logger.log(level,msg);}
    private static String safe(String s){return s==null?"Unknown error":s.replace("\n"," ");}
}
