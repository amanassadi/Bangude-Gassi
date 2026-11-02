import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Team Leader integration: starts UI, Core and Logging as separate Java processes. */
public class IntegrationLauncher {
    public static void main(String[] args) throws Exception {
        Path out=Paths.get("out").toAbsolutePath();
        List<Process> processes=new ArrayList<>();
        Process logger=start(out,"LoggingProcess"); processes.add(logger); Thread.sleep(500);
        Process core=start(out,"CoreProcess"); processes.add(core); Thread.sleep(700);
        Process ui=start(out,"SimulatorUI"); processes.add(ui);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { for(Process p:processes) if(p.isAlive()) p.destroy(); }));
        int code=ui.waitFor();
        for(Process p:processes) if(p.isAlive()) p.destroy();
        System.exit(code);
    }
    private static Process start(Path out,String main) throws IOException {
        ProcessBuilder pb=new ProcessBuilder("java","-cp",out.toString(),main); pb.inheritIO(); return pb.start();
    }
}
