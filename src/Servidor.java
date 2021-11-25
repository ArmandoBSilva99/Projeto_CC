import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);
            Socket socket = serverSocket.accept();
            while (true) {


                System.out.println("New client connected");



                //To send data
                System.out.println("Sending...");
                OutputStream output = socket.getOutputStream();

                PrintWriter writer = new PrintWriter(output, true);


                //List of Files
                File folder = new File(System.getProperty("user.dir"));
                File[] listOfFiles = folder.listFiles();

                StringBuilder sb = new StringBuilder();

                for(File file : listOfFiles) {
                    if(file.isFile()) {
                        //System.out.println(file.getName());
                        sb.append("/" + file.getName()+"\n");
                    }
                }


                String files = sb.toString();
                writer.println(sb);

                Thread.sleep(5000);
            }

        } catch (Exception ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}