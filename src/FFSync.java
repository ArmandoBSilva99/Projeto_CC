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

            DataPacket my_files = new DataPacket();
            my_files.fileListPackets(folder); //List of files in folder
            s.setSoTimeout(10000);
            
            DataPacket allFragments = new DataPacket();
            for(int i = 0; i < my_files.getPackets().size();){
                p.send(s, ip, port, my_files, i);
                System.out.println("Sending my files part " + i);
                try {
                    byte[] frag = p.receive(s);
                    if (frag[0] != 'A') { //Se não for Ack, então é o file dele
                        Packet myAck = new Packet(1);
                        p.sendAck(s, ip, port, myAck); 
                        allFragments.getPackets().add(frag);
                        String sRecv1 = new String(frag, StandardCharsets.UTF_8);
                        String[] splitted = sRecv1.split("\u001C");
                        for(String str: splitted)
                            System.out.println("File before unify:" + str);
                        i++;
                    }
                    else {
                        System.out.println("Ack received");
                    }    
                } catch (SocketTimeoutException e){
                    p.send(s, ip, port, my_files, i);
                    System.out.println("Resending");
                }
            }
            byte[] res = allFragments.unifyBytes();
            String sRecv = new String(res, StandardCharsets.UTF_8);
            String[] splitted = sRecv.split("\u001C");
            for(String str: splitted)
                System.out.println("File :" + str);

            /*
            DataPacket their_files = p.receive(s); //Receber coisas
            byte[] res = their_files.unifyBytes();
            String sRecv = new String(res, StandardCharsets.UTF_8);
            String[] splitted = sRecv.split("\u001C");
            for(String str: splitted)
                System.out.println("File :" + str);
            */

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
