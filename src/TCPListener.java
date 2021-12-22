import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListener implements Runnable {

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(FFSync.MAIN_PORT);
            Socket conn = server.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // don't use buffered writer because we need to write both "text" and "binary"
            OutputStream out = conn.getOutputStream();
            int count = 0;
            while (true) {
                count++;
                String line = reader.readLine();
                if (line == null) {
                    System.out.println("Connection closed");
                    break;
                }
                System.out.println("" + count + ": " + line);
                if (line.equals("")) {
                    System.out.println("Writing response...");

                    // need to construct response bytes first
                    byte[] response = "<html><body>Hello World</body></html>".getBytes("ASCII");

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
            server.close();
        } catch (Exception e) {
        }

    }
}
