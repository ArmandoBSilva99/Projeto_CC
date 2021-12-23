import java.io.IOException;
import java.net.*;
import java.util.List;

/**
 * Classe encarregue de enviar todos os packets armazenados num DataPackets, ou caso
 * receba um filename, cria um novo DataPackets que reparte o ficheiro e envia-o.
 */
public class SendPackets implements Runnable {
    private DataPackets to_send;
    private int port;
    private InetAddress ip;
    String filename;

    public SendPackets(DataPackets to_send, InetAddress ip, int port) {
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
                this.to_send = new DataPackets();
                this.to_send.filePackets(filename);
            }

            List<Packet> data_packets = this.to_send.getPackets();
            long start_time = System.currentTimeMillis();
            int tries = 0;
            for (int i = 0; i < to_send.size() && tries < 5; ) {
                try {
                    s.setSoTimeout(10000); // Mudar
                    byte[] packet_to_send = data_packets.get(i).toBytes();
                    DatagramPacket p = new DatagramPacket(packet_to_send, packet_to_send.length, ip, port);
                    s.send(p);

                    DatagramPacket ack = new DatagramPacket(new byte[DataPackets.PACKET_SIZE], DataPackets.PACKET_SIZE);
                    s.receive(ack);
                    if (Packet.fromBytes(ack.getData()).getSeqNum() == i)
                        i++;
                } catch (SocketTimeoutException ignored) {
                    System.out.println("Retrying");
                    tries++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Packet pac = data_packets.get(0);
            double transfer_time = (System.currentTimeMillis() - start_time) / 1000.0;

            if (pac.getId() == Packet.FILE_ID) {
                int bit_size = this.to_send.byteSize() * 8;
                int bit_per_sec = (int) (bit_size / transfer_time);
                System.out.println(" Filename: " + pac.getNome());
                System.out.println(" Sent with debit: " + bit_per_sec + " bit/s \n");
            }
            s.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
