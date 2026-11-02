import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/** Week 4 Student 3: independent logging process. */
public class LoggingProcess {
    public static final int PORT = 5001;
    private static final Path LOG = Paths.get("execution.log");
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Logging Process listening on port " + PORT);
            while (true) {
                Socket s = server.accept();
                pool.submit(() -> handle(s));
            }
        } finally { pool.shutdownNow(); }
    }
    private static void handle(Socket socket) {
        try (Socket s=socket; BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String line;
            while((line=in.readLine())!=null){
                if("QUIT".equals(line)) break;
                String record=LocalDateTime.now()+" | "+line;
                Files.writeString(LOG, record+System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println(record);
            }
        } catch(IOException e){System.err.println("Logger client error: "+e.getMessage());}
    }
}
