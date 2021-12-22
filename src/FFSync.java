import java.net.InetAddress;
import java.net.UnknownHostException;

public class FFSync {
    public static final int MAIN_PORT = 80;

    public static void main(String args[]) throws UnknownHostException {
        String folder = args[0];
        String ip = args[1];

        //TCP THREAD
        TCPListener tcpl = new TCPListener(folder);
        Thread tcp = new Thread(tcpl);
        tcp.start();
        try {
            tcp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        //UDP THREAD
        ThreadedFTRapidProtocol dsa = new ThreadedFTRapidProtocol(folder,
        InetAddress.getByName(ip));
        
        Thread udp = new Thread(dsa);
        udp.start();
        try {
            udp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

}
