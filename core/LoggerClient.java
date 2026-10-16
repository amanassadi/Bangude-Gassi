import java.io.*;
import java.net.Socket;

/** Small Core -> Logger TCP client. */
public class LoggerClient {
    private Socket socket;
    private BufferedWriter out;
    public void connect() throws IOException {
        socket = new Socket("127.0.0.1", 5001);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    public synchronized void log(String level, String message) {
        if (out == null) return;
        try { out.write(level + "|" + message.replace("\n", " ")); out.newLine(); out.flush(); }
        catch (IOException ignored) { }
    }
    public void close(){try{if(out!=null){out.write("QUIT");out.newLine();out.flush();}if(socket!=null)socket.close();}catch(IOException ignored){}}
}
