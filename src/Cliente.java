import java.io.*;
import java.net.Socket;

public class Cliente {

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {

            while(true) {

                //To receive data
                System.out.println("Waiting...");
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String textFromServer;
                while ((textFromServer = reader.readLine()) != null)
                    System.out.println(textFromServer);

                Thread.sleep(5000);
            }
        } catch (Exception ex) {

            System.out.println("Server not found: " + ex.getMessage());

        }
    }
}