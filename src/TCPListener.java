import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

public class TCPListener implements Runnable {

    private String filepath;

    TCPListener(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(FFSync.MAIN_PORT);
            // don't use buffered writer because we need to write both "text" and "binary"
            int count = 0;
            Socket conn = null;
            while (true) {
                server.setSoTimeout(30000);
                conn = server.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                OutputStream out = conn.getOutputStream();
                while (true) {


                    count++;
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println("" + count + ": " + line);
                    if (line.equals("")) {

                        Map<String, FileInfo> my_files = FileInfo.getDirFileInfo(this.filepath);
                        StringBuilder files = new StringBuilder();
                        if (my_files.size() != 0) {
                            for (String s : my_files.keySet()) {
                                files.append(s + "\n");
                            }
                        } else files.append("Wrong folder\n");

                        // need to construct response bytes first
                        byte[] response = ("<html><body>\n" + files.toString() + "</body></html>").getBytes("ASCII");

                        String statusLine = "HTTP/1.1 200 OK\r\n";
                        out.write(statusLine.getBytes("ASCII"));

                        String contentLength = "Content-Length: " + response.length + "\r\n";
                        out.write(contentLength.getBytes("ASCII"));

                        // signal end of headers
                        out.write("\r\n".getBytes("ASCII"));

                        // write actual response and flush
                        out.write(response);
                        out.flush();
                    }

                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Stopped receiving HTTP requests");
        } catch (Exception e) {
        }

    }
}
