import java.io.*;
import java.net.Socket;
import java.util.*;

/** Week 4 adapter: keeps the Week 3 UI API while execution is performed by CoreProcess. */
public class RemoteCPU extends CPU {
    private final boolean initializing;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean halted = false;
    private int pc = 0, sp = CPU.STACK_BASE, a = 0, b = 0;
    private int[] regs = new int[8];
    private boolean cy, ac, ov, p;
    private long cycles;
    private final Memory mirror = new Memory();
    private final List<Integer> stackMirror = new ArrayList<>();
    private final List<Integer> queueMirror = new ArrayList<>();
    private int queueStatus = FifoQueue.STATUS_EMPTY;

    public RemoteCPU() {
        super();
        initializing = false;
        connect();
    }

    private void connect() {
        try {
            socket = new Socket("127.0.0.1", 5000);
            socket.setSoTimeout(15000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            request("PING");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot connect to Core Process. Start IntegrationLauncher first.");
        }
    }

    private synchronized List<String> request(String command) {
        try {
            out.write(command.replace("\n", "\\n"));
            out.newLine(); out.flush();
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END")) break;
                lines.add(line);
            }
            for (String s : lines) apply(s);
            return lines;
        } catch (IOException e) {
            throw new IllegalStateException("Core connection failed: " + e.getMessage(), e);
        }
    }

    private void apply(String s) {
        if (s.startsWith("STATE|")) {
            String[] p = s.split("\\|");
            for (String x : p) {
                int k=x.indexOf('='); if(k<0) continue;
                String key=x.substring(0,k), val=x.substring(k+1);
                int n;
                try { n=Integer.parseInt(val); } catch(Exception e){ continue; }
                switch(key){case "PC":pc=n;break;case "SP":sp=n;break;case "A":a=n;break;case "B":b=n;break;case "CYCLES":cycles=n;break;}
            }
        } else if (s.startsWith("REGS|")) {
            String[] p=s.split("\\|");
            for(String x:p){int k=x.indexOf('=');if(k<0)continue;String key=x.substring(0,k);try{int n=Integer.parseInt(x.substring(k+1));if(key.matches("R[0-7]"))regs[Integer.parseInt(key.substring(1))]=n;}catch(Exception ignored){}}
        } else if (s.startsWith("FLAGS|")) {
            String[] parts=s.split("\\|"); for(String x:parts){int k=x.indexOf('=');if(k<0)continue;boolean v="1".equals(x.substring(k+1));switch(x.substring(0,k)){case "CY":cy=v;break;case "AC":ac=v;break;case "OV":ov=v;break;case "P":this.p=v;break;}}
        } else if (s.startsWith("MEM|")) {
            String[] p=s.substring(4).split(","); for(String x:p){String[] q=x.split(":");if(q.length==2)try{mirror.poke(Integer.parseInt(q[0]),Integer.parseInt(q[1]));}catch(Exception ignored){}}
        } else if (s.startsWith("STACK|")) {
            stackMirror.clear(); String body=s.substring(6); if(!body.isEmpty())for(String x:body.split(","))try{stackMirror.add(Integer.parseInt(x));}catch(Exception ignored){}
        } else if (s.startsWith("QUEUE|")) {
            queueMirror.clear(); String body=s.substring(6); if(!body.isEmpty()){String[] q=body.split(";");try{queueStatus=Integer.parseInt(q[0]);}catch(Exception ignored){}for(int i=1;i<q.length;i++)try{queueMirror.add(Integer.parseInt(q[i]));}catch(Exception ignored){}}
        } else if (s.equals("HALTED")) halted=true;
        else if (s.startsWith("STATUS|HALTED")) halted=true;
    }

    public void loadSource(String source) { halted=false; request("LOADSRC|"+Base64.getEncoder().encodeToString(source.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
    @Override public void loadProgram(List<Instruction> instructions) { StringBuilder b=new StringBuilder(); for(int i=0;i<instructions.size();i++){if(i>0)b.append('\n'); b.append(instructions.get(i).toString());} loadSource(b.toString()); }
    private static String escape(String s){return s.replace("|","%7C").replace(";","%3B");}
    @Override public Instruction fetch(){ return halted || pc<0 ? null : new Instruction("REMOTE"); }
    @Override public void step(){ List<String> r=request("STEP"); halted=false; for(String s:r)if(s.startsWith("STATUS|HALTED"))halted=true; }
    @Override public void run(){ List<String> r=request("RUN"); for(String s:r)if(s.startsWith("STATUS|HALTED")||s.equals("HALTED"))halted=true; }
    @Override public void reset(){ if(out==null){ super.reset(); return; } request("RESET"); pc=0;sp=CPU.STACK_BASE;a=b=0;cycles=0;halted=false;Arrays.fill(regs,0);mirror.clear();stackMirror.clear();queueMirror.clear();queueStatus=FifoQueue.STATUS_EMPTY; }
    @Override public int getA(){return a;} @Override public int getB(){return b;} @Override public int getPC(){return pc;} @Override public int getSP(){return sp;} @Override public long getCycles(){return cycles;}
    @Override public int getRegisterValue(String r){if(r.startsWith("R"))try{return regs[Integer.parseInt(r.substring(1))];}catch(Exception ignored){} return 0;}
    @Override public boolean getCY(){return cy;} @Override public boolean getAC(){return ac;} @Override public boolean getOV(){return ov;} @Override public boolean getP(){return p;} @Override public boolean isHalted(){return halted;}
    @Override public Memory getMemory(){return mirror;}
    @Override public int[] getStackContents(){int[] a=new int[stackMirror.size()];for(int i=0;i<a.length;i++)a[i]=stackMirror.get(i);return a;}
    public List<Integer> getQueueValues(){return new ArrayList<>(queueMirror);} public int getQueueStatus(){return queueStatus;}
    @Override public int getProgramSize(){return 9999;} // UI uses its own assembled program list.
    public void close(){try{if(out!=null){out.write("QUIT");out.newLine();out.flush();}if(socket!=null)socket.close();}catch(IOException ignored){}}
}
