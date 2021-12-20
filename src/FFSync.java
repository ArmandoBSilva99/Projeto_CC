import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
public class FFSync {
    public static void main(String args[]) {
        DatagramSocket s;
        FTRapidProtocol p = new FTRapidProtocol();
        int port = 8888;
        String folder = args[0];
        String ip = args[1];

        // |---------------------------|
        // |   Isto funciona Sempre!!! |
        // |---------------------------|

        try {
            s = new DatagramSocket(port);
            Packet packConn = new Packet(0);
            p.connCheck(s, ip, port, packConn); //Começa a conexão, i.e, verifica se estão comunicáveis 
            DataPacket my_files = new DataPacket();
            my_files.fileListPackets(folder); //List of files in folder
            
            DataPacket allFragments = new DataPacket();

            int size = 10000; //Tamanho arbitrário que depois é alterado no for (numero de partições)
            int their_file_max = 10000; //Tamanho arbitrário que depois é alterado no for (numero de acks a dar)
            int file_max = my_files.getPackets().size();

            

            for(int i = 0, j = 0; (i < file_max || j < their_file_max);){
    
                s.setSoTimeout(500);
                
                if (i < file_max) p.send(s, ip, port, my_files, i); //Envia enquanto houver partições para enviar

                boolean ackreceived = false; //Booleano que verifica se recebeu o ack
                boolean filereceived = false; //Booleano que verifica se recebeu o ficheiro 

                while ((!ackreceived) || (!filereceived)) {
                   
                    try {
                        byte[] frag = p.receive(s);
                        if (frag[0] != 'A') { //Se não for Ack, então é o file dele

                            j++;

                            Packet packk1 = Packet.fromBytes(frag);
                            //System.out.println("Got it file " + packk1.getSeqNum()); //Debugging

                            their_file_max = packk1.getNPack();   
                            
                            Packet myAck = new Packet(packk1.getSeqNum());
                            p.sendAck(s, ip, port, myAck); 
                            allFragments.getPackets().add(frag);

                            filereceived = true;
                            
                            if (i >= file_max)
                                ackreceived = true;

                        }
                        if (frag[0] == 'A'){ //Recebi um Ack  

                            Packet packk2 = Packet.fromBytes(frag);
                            //System.out.println("Got it Ack " + packk2.getSeqNum()); //Debugging
                            
                            if (i == packk2.getSeqNum())
                                ackreceived = true;

                            if (j >= their_file_max)
                                filereceived = true;    
                            
                            i++;
                        }

                        
                    } catch (SocketTimeoutException e) {
                        System.out.println("Retrying");         

                        if (i < my_files.getPackets().size() && !ackreceived) { //Enquanto houver partições para enviar e ainda não ter recebido esse Ack
                            System.out.println("Sending again...");
                            p.send(s, ip, port, my_files, i); 
                        }    
                    }
                    
                }    
            }
            
            
            //Os ficheiros que o outro tem
            byte[] res = allFragments.unifyBytes();
            String sRecv = new String(res, StandardCharsets.UTF_8);
            
            /*
            String[] resultt = sRecv.split("\u001C");
            for(String str: resultt){
                System.out.println("File: " + str);
            }
            */

            /*
            //Os ficheiros que me faltam
            String missing_files = FileInfo.missingFiles(folder, sRecv);
            String[] result = missing_files.split("\u001C");
            for(String str: result){
                System.out.println("Missing files: " + str);
            }
            */

            DataPacket miss = new DataPacket();
            miss.missingFileListPackets(folder, sRecv);
            
                
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
