import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class SendPackets implements Runnable {
    private DataPacket to_send;
    private int port;
    private InetAddress ip;
    String filename;

    public SendPackets(DataPacket to_send, InetAddress ip, int port) {
        this.to_send = to_send;
        this.ip = ip;
        this.port = port;
        this.filename = null;
    }

    public SendPackets(String filename, InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            DatagramSocket s = new DatagramSocket();
            if (filename != null) {
                this.to_send = new DataPacket();
                this.to_send.filePackets(filename);
            }

            List<Packet> data_packets = this.to_send.getPackets();

            for (int i = 0; i < to_send.size(); ) {
                try {
                    s.setSoTimeout(10000);                                      // Mudar
                    byte[] packet_to_send = data_packets.get(i).toBytes();
                    DatagramPacket p = new DatagramPacket(packet_to_send, packet_to_send.length, ip, port);
                    s.send(p);

                    s.receive(p);
                    i++;
                } catch (SocketTimeoutException ignored) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }
    }
}
