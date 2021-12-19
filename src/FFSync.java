import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SizeRequirements;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class FFSync {
    public static void main(String args[]) {
        DatagramSocket s;
        FTRapidProtocol p = new FTRapidProtocol();
        int port = 8888;
        String folder = args[0];
        String ip = args[1];


        try {
            s = new DatagramSocket(port);
            int i = 0;

            DataPacket pack1 = new DataPacket();
            pack1.fileListPackets(folder); //List of files in folder

            p.send(s, ip, port, pack1);
            System.out.println("sent");

            s.setSoTimeout(10000);


            DataPacket pack2 = p.receive(s); //Receber coisas
            byte[] res = pack2.unifyBytes();
            String sRecv = new String(res, StandardCharsets.UTF_8);
            System.out.println("Mensagem recebida: " + sRecv + "\n");
            i++;

                /*
                if (recv.size() != 0 && (recv.get(0))[0] != 'A') { //Se não for Ack
                    String sRecv = new String(recv.get(0), StandardCharsets.UTF_8);
                    System.out.println("Mensagem recebida: " + sRecv);
                    //Send ack..
                    PacketHeader ack = new PacketHeader(1);
                    p.sendAck(s, ip, port, ack); //Enviar Ack

                }
                else { //Se for Ack
                    System.out.println("Packet not received.");
                }
            */
            //DataPacket ack2 = p.receive(s);


            //p.connCheck(s,ip,port,pack1, ack);
            /*
            SimplePacket pack3 = missingFiles(message,sRecv);  
            p.send(s, ip, port, pack3);

            SimplePacket pack4 = p.receive(s);
            pack4.printSimplePacket();

            //Send missing files...
            //Receive missing files...
            */
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
