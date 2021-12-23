import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FFSync {
    public static final int MAIN_PORT = 80;

    public static void main(String[] args) {
        String folder = args[0];
        String ip = args[1];

        File f = new File(folder);
        if (!f.isDirectory()) {
            System.out.println("Folder doesn't exist!");
            return;
        }

        //TCP THREAD
        TCPListener tcpl = new TCPListener(folder);
        Thread tcp = new Thread(tcpl);
        //UDP THREAD
        FTRapidProtocol FTRapid = null;
        try {
            FTRapid = new FTRapidProtocol(folder, InetAddress.getByName(ip));
        } catch (UnknownHostException e) {
            System.out.println("Host not found!");
        }
        Thread udp = new Thread(FTRapid);
        udp.start();
        tcp.start();
        try {
            udp.join();
            tcp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
