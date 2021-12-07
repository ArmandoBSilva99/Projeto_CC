import java.net.DatagramSocket;
import java.net.InetAddress;


public class FFSync {
    public static void main(String[] args) throws Exception{ 
        FTRapidProtocol protocol = new FTRapidProtocol();
        DatagramSocket s = new DatagramSocket(8888);
        protocol.start(s, args[0], 8888, args[1], args[2]);
        //Info info = new Info(80, "localhost", args[0], args[1]);
        //info.printFiles();
    }
}
