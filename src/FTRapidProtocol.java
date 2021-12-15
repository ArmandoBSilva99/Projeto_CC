import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class FTRapidProtocol {

    //Probably inutil?
    /*public void start(DatagramSocket s, String hostS, int port, String message) throws IOException, DateTimeParseException{
        InetAddress address = s.getLocalAddress();
        HostInfo f = new HostInfo(port, address.getHostAddress());
        //String message = f.myFiles();
        //Map<String,FileInfo> myInfo = parse(message);
        /*Map<String,FileInfo> map = f.parse(f.myFiles());
        for (Map.Entry<String, FileInfo> infos : map.entrySet()){
            FileInfo fi = infos.getValue();
            System.out.println("Nome: " + fi.getName() + " Data: " + fi.getData());
        }
        //
        byte[] m = message.getBytes();
        SimplePacket p = new SimplePacket(0,0,m); 
        send(s, hostS, port, p);
        System.out.println("sent Servidor1");
        s.setSoTimeout(10000);

        SimplePacket ack = receive(s);
        //readMessageType(ack.getMsgType(),ack);
        //Mensagem recebida
        byte[] recv = ack.getData();
        String sRecv = new String(recv, StandardCharsets.UTF_8);
        System.out.println("Mensagem recebida: " + sRecv);
        //Map<String,FileInfo> otherInfo = parse(sRecv);

        System.out.println("Servidor 1 diz: " + ack.getAck());
        if (ack.getAck() == 0) {
            p.setAck(1);
            System.out.println("Not Acknowledged\nSending again...");
            send(s, hostS, port, p);

            System.out.println("Connection Established");

        }
        printSimplePacket(ack);

        SimplePacket pack = missingFiles(message,sRecv);  
        send(s, hostS, port, pack);

        SimplePacket ans = receive(s);
        printSimplePacket(ans);

        //send File...
    }
    */

    //To establish connection
    public void connCheck(DatagramSocket s, String hostS, int port, SimplePacket p, SimplePacket ack) throws IOException{
        System.out.println("Servidor 1 diz: " + ack.getAck());
        if (ack.getAck() == 0) {
            p.setAck(1);
            System.out.println("Not Acknowledged\nSending again...");
            send(s, hostS, port, p);

            System.out.println("Connection Established");

        }
        ack.printSimplePacket();
    }

    //Send Packets
    public void send(DatagramSocket s, String hostS, int port, SimplePacket p) throws IOException{
        InetAddress host = InetAddress.getByName(hostS);
        byte[] m = p.packetToBytes();
        System.out.println("Servidor a enviar para " + host);
        //System.out.println("Tamanho m: " + m.length);
        s.send(new DatagramPacket(m, m.length, host, port)); 
    }

    //Receive Packets
    public SimplePacket receive(DatagramSocket s) throws IOException{
        byte[] packetBytes = new byte[1024];
        DatagramPacket dp = new DatagramPacket(packetBytes, packetBytes.length);
        s.receive(dp);
        SimplePacket pacote = new SimplePacket(1);
        pacote.bytesToPacket(packetBytes);
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