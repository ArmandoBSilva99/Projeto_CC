import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;


public class FTRapidProtocol {

    
    //To establish connection (Mandar merdas random só para estabelecer conexão)
    public int connCheck(DatagramSocket s, String hostS, int port, Packet p) throws IOException {
        for (int i = 1; i < 5; i++)
            if (safetyMeasure(s, hostS, port)) {
                System.out.println("Connection Established");
                return 0;
            } else {
                System.out.println("Wrong shared password... Try Again (" + i + "/3)");
        }
        System.out.println("Ran out of tries");
        return -1;        
    }
    
    public boolean safetyMeasure(DatagramSocket s, String hostS, int port) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        //boolean sharedPassword = false;
        System.out.println("Insert your Shared Password: ");
        String pass = reader.readLine();
        byte[] m = pass.getBytes();
        Packet p = new Packet(Packet.PASS_ID, m.length, 1, 0, m);
        for(int i = 0; i < 3;) {
            s.setSoTimeout(3000);
            sendAck(s, hostS, port, p);
            try {
                byte[] rs = receive(s);
                Packet received = Packet.fromBytes(rs);
                if (received.getId() == Packet.PASS_ID) {
                    String their_pass = new String(received.getData(), StandardCharsets.UTF_8);
                    if (pass.equals(their_pass)) return true;
                    else return false;
                }    
            } catch (SocketTimeoutException e) {
                sendAck(s, hostS, port, p);
            }
        }
        return false;
    }
    
    //Send Packets
    public void send(DatagramSocket s, String hostS, int port, DataPacket p, int index) throws IOException{
        InetAddress host = InetAddress.getByName(hostS);
        //System.out.println("Servidor a enviar para " + host);
        Packet ps = p.getPackets().get(index);
        byte[] m = ps.toBytes();
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

    public DataPacket sendListOfPackages(DatagramSocket s, String ip, int port, DataPacket my_files) throws IOException, ExceededTriesException{
        
        DataPacket allFragments = new DataPacket();

        int their_file_max = 10000; //Tamanho arbitrário que depois é alterado no for (numero de acks a dar)
        //System.out.println("Going in for size");
        int file_max = my_files.getPackets().size(); //Número de partições que a minha lista com os nomes dos ficheiros tem
        //System.out.println("Going in if");
        
        Packet temp = my_files.getPackets().get(0); //Averigua se é um pacote com informação ou apenas um Ack 
        
        //System.out.println("Going in if if");
        //System.out.println("data:" + temp.getData());
        //System.out.println("tamanho data:" + temp.getData().length);
        //if (temp.getData().length == 0) file_max = 0;
        if(temp.getData() == null) file_max = 0;
        
        //System.out.println("Going in for");
        
        for(int i = 0, j = 0; (i < file_max || j < their_file_max);){
            System.out.println("I: " + i);
            s.setSoTimeout(2000);
            //System.out.println("filemax " + file_max + " i: " + i);
            
            if (i < file_max || (file_max == 0 && i == 0)) { //Só envia se tiver algo para enviar ou então envia apenas a informação do número de partições que tem
                    System.out.println("Ready to Send");
                    send(s, ip, port, my_files, i); //Envia enquanto houver partições para enviar
                    //System.out.println("Sending");  
            }    
            
            boolean ackreceived = false; //Booleano que verifica se recebeu o ack
            boolean filereceived = false; //Booleano que verifica se recebeu o ficheiro 
            int tries = 0;
            System.out.println("Going in while");
            while ((!ackreceived) || (!filereceived)) {
                System.out.println("In while");
                if (tries == 5) 
                    throw new ExceededTriesException("Tries Exceeded. Please Try Again");
                try {
                    byte[] frag = receive(s);
                    Packet pack = Packet.fromBytes(frag);
                    if (pack.getId() != Packet.ACK_ID && pack.getId() != Packet.PASS_ID) { //Se não for Ack, então é o file dele
                        System.out.println("Got frag: " + pack.getSeqNum());
                        j++;
                        
                        //Packet packk = Packet.fromBytes(frag);
                        //System.out.println("from Bytes");
                        //System.out.println("Got it file " + packk1.getSeqNum()); //Debugging

                        /*
                        String rev = new String(frag, StandardCharsets.UTF_8);
                        System.out.println("File: " + rev);
                        */

                        their_file_max = pack.getNPack(); //Número de partições que o outro servidor tem para enviar
                        
                        Packet myAck = new Packet(pack.getSeqNum()); //Envia o ack X de que recebeu a partição X
                        sendAck(s, ip, port, myAck); 
                        
                        System.out.println("Seq= " + pack.getSeqNum());
                        if (allFragments.getPackets().size() < pack.getSeqNum() + 1) allFragments.getPackets().add(pack);
                        else allFragments.getPackets().set(pack.getSeqNum(), pack); //Vai adicionando a um DataPacket à medida que recebe

                        filereceived = true;
                        
                        if (i >= file_max) //Este if serve para caso já não tenha Acks para receber (pois este servidor já enviou tudo) não ficar preso no while
                            ackreceived = true; 

                    }
                    if (pack.getId() == Packet.ACK_ID){ //Recebi um Ack  
                        System.out.println("Got Ack: " + pack.getSeqNum());
                       //Packet packk2 = Packet.fromBytes(frag);
                        //System.out.println("from Bytes");
                        //System.out.println("Got it Ack " + packk2.getSeqNum()); //Debugging
                       

                        //System.out.println("I: " + i + " SeqNum: " + pack.getSeqNum());
                        if (i == pack.getSeqNum()){ 
                                               //Só muda a flag ackreceived quando receber o ack da partição que recebeu 
                            ackreceived = true;
                            
                        //    System.out.println("If Accessed");
                        }    
                        System.out.println("J: " + j);
                        System.out.println("their file_max: " + their_file_max);
                        if (j >= their_file_max) //Este if serve para caso já não tenha partições para receber (pois o outro servidor já enviou tudo) não ficar preso no while
                            filereceived = true;    
                       
                        
                    }

                    
                } catch (SocketTimeoutException e) {
                    System.out.println("Retrying i: " + i);         
                    tries++;
                    if ((i < file_max && !ackreceived) || (file_max == 0 && i == 0)) {
                        //System.out.println("Ready to Send"); //Enquanto houver partições para enviar e ainda não ter recebido esse Ack
                        send(s, ip, port, my_files, i); 
                        //System.out.println("Sending again...");
                    }    
                }
            if (filereceived && ackreceived) i++;    
            }    
        }
        return allFragments;    
    }

    public void sendNReceiveFiles(DatagramSocket ds, String ip, int port, String str_files, String filepath, int nPack) throws IOException, ExceededTriesException{
        int i = 0;

        if (str_files.length() != 0) { //Se tem ficheiros para enviar
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
                //System.out.println("i: " + i);
            }
        }
        while (i < nPack) { //Se não tem ficheiros para enviar mas sim para receber
            //System.out.println("in second if");
            DataPacket a = new DataPacket();
            Packet mis = new Packet(Packet.MISSING_ID); 
            //System.out.println("To bytes");
            a.getPackets().add(mis); 
            //System.out.println("sendListOfPackages");
            DataPacket b = sendListOfPackages(ds, ip, port, a);
            //System.out.println("apos datapacket B");
            b.dataPacketToFile(filepath);
            i++;
            //System.out.println("i: " + i);
            System.out.println("apos datapacket to file");
        }
    }
}    