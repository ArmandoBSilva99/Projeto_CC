import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FFSync {
    public static final int MAIN_PORT = 80;

    public static void main(String args[]) throws UnknownHostException {
        String folder = args[0];
        String ip = args[1];

        File f = new File(folder);
        if (!f.isDirectory()) {
            System.out.println("Folder doesn't exist!");
            return;
        }
/*
        //TCP THREAD
        TCPListener tcpl = new TCPListener(folder);
        Thread tcp = new Thread(tcpl);
        tcp.start();
        try {
            tcp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        //UDP THREAD
        FTRapidProtocol dsa = new FTRapidProtocol(folder, InetAddress.getByName(ip));

        Thread udp = new Thread(dsa);
        udp.start();
        try {
            udp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
