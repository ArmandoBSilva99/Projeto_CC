import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;


public class FTRapidProtocol {

    
    //To establish connection (Mandar merdas random só para estabelecer conexão)
    public void connCheck(DatagramSocket s, String hostS, int port, Packet p) throws IOException{
        sendAck(s, hostS, port, p);
        s.setSoTimeout(3000);
        byte[] ack = receive(s); 
        Packet pAck = Packet.fromBytes(ack);
        if (pAck.getSeqNum() == 0) {
            System.out.println("Connection Established");
        }
    }
    
    
    //Send Packets
    public void send(DatagramSocket s, String hostS, int port, DataPacket p, int index) throws IOException{
        InetAddress host = InetAddress.getByName(hostS);
        //System.out.println("Servidor a enviar para " + host);
        byte[] m = p.getPackets().get(index);
        s.send(new DatagramPacket(m, m.length, host, port));
    }

    public void sendAck(DatagramSocket s, String hostS, int port, Packet p) throws IOException{
        InetAddress host = InetAddress.getByName(hostS);
        //System.out.println("Servidor a enviar para " + host);
        byte[] m = p.toBytes();
        //System.out.println("In bytes ready to send");
        s.send(new DatagramPacket(m, m.length, host, port));      
        //System.out.println("Sent");  
    }

    //Receive Packets
    public byte[] receive(DatagramSocket s) throws IOException{
        byte[] packetBytes = new byte[DataPacket.packet_size];
        DatagramPacket dp = new DatagramPacket(packetBytes, packetBytes.length);
        s.receive(dp);
        return packetBytes;
    }

    public DataPacket sendListOfPackages(DatagramSocket s, String ip, int port, DataPacket my_files) throws IOException{
        
        DataPacket allFragments = new DataPacket();

        int their_file_max = 10000; //Tamanho arbitrário que depois é alterado no for (numero de acks a dar)
        //System.out.println("Going in for size");
        int file_max = my_files.getPackets().size();
        //System.out.println("Going in if");
        
        byte[] m = my_files.getPackets().get(0);
        Packet temp = Packet.fromBytes(m);
        
        //System.out.println("Going in if if");
        //System.out.println("data:" + temp.getData());
        //System.out.println("tamanho data:" + temp.getData().length);
        //if (temp.getData().length == 0) file_max = 0;
        if(temp.getData() == null) file_max = 0;
        
        //System.out.println("Going in for");

        for(int i = 0, j = 0; (i < file_max || j < their_file_max);){

            s.setSoTimeout(500);
            //System.out.println("filemax " + file_max + " i: " + i);
            
            if (i < file_max || (file_max == 0 && i == 0)) {
                    //System.out.println("Ready to Send");
                    send(s, ip, port, my_files, i); //Envia enquanto houver partições para enviar
                    //System.out.println("Sending");  
            }    
            
            boolean ackreceived = false; //Booleano que verifica se recebeu o ack
            boolean filereceived = false; //Booleano que verifica se recebeu o ficheiro 

            //System.out.println("Going in while");
            while ((!ackreceived) || (!filereceived)) {
               
                try {
                    byte[] frag = receive(s);
                    Packet packk = Packet.fromBytes(frag);
                    if (packk.getId() != 'A') { //Se não for Ack, então é o file dele
                        System.out.println("Got file");
                        j++;
                        
                        //Packet packk = Packet.fromBytes(frag);
                        //System.out.println("from Bytes");
                        //System.out.println("Got it file " + packk1.getSeqNum()); //Debugging

                        /*
                        String rev = new String(frag, StandardCharsets.UTF_8);
                        System.out.println("File: " + rev);
                        */

                        their_file_max = packk.getNPack();   
                        
                        Packet myAck = new Packet(packk.getSeqNum());
                        sendAck(s, ip, port, myAck); 
                        allFragments.getPackets().add(frag);

                        filereceived = true;
                        
                        if (i >= file_max)
                            ackreceived = true;

                    }
                    if (packk.getId() == 'A'){ //Recebi um Ack  
                        //System.out.println("Got Ack");
                       //Packet packk2 = Packet.fromBytes(frag);
                        //System.out.println("from Bytes");
                        //System.out.println("Got it Ack " + packk2.getSeqNum()); //Debugging
                        
                        //System.out.println("Accessing if");
                        if (i == packk.getSeqNum()){
                            ackreceived = true;
                        //    System.out.println("If Accessed");
                        }    

                        if (j >= their_file_max)
                            filereceived = true;    
                        
                        i++;
                    }

                    
                } catch (SocketTimeoutException e) {
                    //System.out.println("Retrying");         

                    if ((i < file_max && !ackreceived) || (file_max == 0 && i == 0)) {
                        //System.out.println("Ready to Send"); //Enquanto houver partições para enviar e ainda não ter recebido esse Ack
                        send(s, ip, port, my_files, i); 
                        //System.out.println("Sending again...");
                    }    
                }
                
            }    
        }
        return allFragments;    
    }

    public void sendNReceiveFiles(DatagramSocket ds, String ip, int port, String str_files, String filepath, int nPack) throws IOException{
        int i = 0;

        if (str_files.length() != 0) {
            String[] splitted = str_files.split(FileInfo.file_separator);
            //System.out.println("Split");
            for(String s: splitted) {
                DataPacket file = new DataPacket();
                String res = filepath + "/" + s;
                //System.out.println("res: " + res);
                file.filePackets(res);
                DataPacket new_file = sendListOfPackages(ds, ip, port, file);
                //System.out.println("Before file");
                new_file.dataPacketToFile(filepath);
                //System.out.println("New file");
                i++;
                System.out.println("i: " + i);
            }
        }
        while (i < nPack) {
            //System.out.println("in second if");
            DataPacket a = new DataPacket();
            Packet mis = new Packet('M');
            //System.out.println("To bytes");
            a.getPackets().add(mis.toBytes()); 
            //System.out.println("sendListOfPackages");
            DataPacket b = sendListOfPackages(ds, ip, port, a);
            //System.out.println("apos datapacket B");
            b.dataPacketToFile(filepath);
            i++;
            System.out.println("i: " + i);
            System.out.println("apos datapacket to file");
        }
    }
}    