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
            int tries = 0;
            for (int i = 0; i < to_send.size() && tries < 5;) {
                try {
                    s.setSoTimeout(10000); // Mudar
                    System.out.println("Sending from : " + Thread.currentThread() + " " + data_packets.get(i).getId()
                            + " " + data_packets.get(i).getSeqNum());
                    byte[] packet_to_send = data_packets.get(i).toBytes();
                    DatagramPacket p = new DatagramPacket(packet_to_send, packet_to_send.length, ip, port);
                    s.send(p);
                    if (data_packets.get(i).getId() == Packet.FILE_ID)
                        System.out.println("Filename: " + data_packets.get(i).getNome());
                    DatagramPacket ack = new DatagramPacket(new byte[DataPacket.PACKET_SIZE], DataPacket.PACKET_SIZE);
                    s.receive(ack);
                    System.out.println("ACK " + Packet.fromBytes(p.getData()).getId() + " "
                            + Packet.fromBytes(p.getData()).getSeqNum());
                    i++;
                    System.out.println("Data Length: " + p.getData().length);
                } catch (SocketTimeoutException ignored) {
                    System.out.println("Thread Expired");
                    tries++;
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