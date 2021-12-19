import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class FTRapidProtocol {

    //To establish connection
    public void connCheck(DatagramSocket s, String hostS, int port, DataPacket p, DataPacket ack) throws IOException{
        //System.out.println("Servidor 1 diz: " + ack.getAck());
        
        System.out.println("Not Acknowledged\nSending again...");
        send(s, hostS, port, p);

        System.out.println("Connection Established");
        
    }

    //Send Packets
    public void send(DatagramSocket s, String hostS, int port, DataPacket p) throws IOException{
        InetAddress host = InetAddress.getByName(hostS);
        System.out.println("Servidor a enviar para " + host);
        //System.out.println("Tamanho m: " + m.length);
        for(byte[] m : p.getPackets()) {
            s.send(new DatagramPacket(m, m.length, host, port));
        }
    }

    public void sendAck(DatagramSocket s, String hostS, int port, Packet p) throws IOException{
        InetAddress host = InetAddress.getByName(hostS);
        System.out.println("Servidor a enviar para " + host);
        //System.out.println("Tamanho m: " + m.length);
        byte[] m = p.toBytes();
        s.send(new DatagramPacket(m, m.length, host, port));        
    }

    //Receive Packets
    public DataPacket receive(DatagramSocket s) throws IOException{
        byte[] packetBytes = new byte[DataPacket.packet_size];
        DatagramPacket dp = new DatagramPacket(packetBytes, packetBytes.length);
        s.receive(dp);
        DataPacket pacote = new DataPacket();
        pacote.getPackets().add(packetBytes);
        return pacote;
    }

    //Read MT
    public void readMessageType(int msgType, SimplePacket p) {
        if(msgType == 0) { //comparar listas
            byte[] recv = p.getData();
            String sRecv = new String(recv, StandardCharsets.UTF_8);
        }
        else if(msgType == 1); //enviar ficheiro em falta
        else if (msgType == 2); //...
        else ; //etc.
    }

}    