import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FTRapidProtocol {

    public void start(DatagramSocket s, String host, int port, String folder, String peer) throws Exception {
        InetAddress address = InetAddress.getByName(peer);
        Info i = new Info(s.getLocalPort(), address.getHostAddress(), folder, peer);
        String m = i.printFiles().toString();
        while(true){
            send(s, port, m, address);
            Thread.sleep(5000);
        }    
    }

    public void send(DatagramSocket s, int port, String message, InetAddress peer){
        try {
            InetAddress host = peer;
            byte[] m = message.getBytes();
            s.send(new DatagramPacket(m, m.length, host, port));
        } catch (IOException e) {
            System.out.println("IO Error");
        }
    
    }


}
