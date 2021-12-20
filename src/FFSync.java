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

        // |-----------------------------------------------------------------------------------------------------------------------|
        // |   Isto funciona quando ambos os servidores têm o mesmo número de partições, caso não tenham, o programa borra-se todo |
        // |-----------------------------------------------------------------------------------------------------------------------|

        try {
            s = new DatagramSocket(port);
            Packet packConn = new Packet(1);
            p.connCheck(s, ip, port, packConn); //Começa a conexão, i.e, verifica se estão comunicáveis 
            DataPacket my_files = new DataPacket();
            my_files.fileListPackets(folder); //List of files in folder
            
            DataPacket allFragments = new DataPacket();

            int size = 10000; //Tamanho arbitrário que depois é alterado no for (numero de partições)
            int ackmax = 10000; //Tamanho arbitrário que depois é alterado no for (numero de acks a dar)

            int filecounter = 0; // Inteiro para controlar os sends e receives
            int ackcounter = 0; // Inteiro para controlar os sends e receives

            for(int i = 0; i < size; i++){
                s.setSoTimeout(500);
                if (i < my_files.getPackets().size()) p.send(s, ip, port, my_files, i); //Envia enquanto houver partições para enviar

                boolean ackreceived = false; //Booleano que verifica se recebeu o ack
                boolean filereceived = false; //Booleano que verifica se recebeu o ficheiro
                
                while ((!ackreceived) || (!filereceived)) {
                   
                    try {
                        byte[] frag = p.receive(s);
                        if (frag[0] != 'A') { //Se não for Ack, então é o file dele
                            System.out.println("Got it"); //Debugging
                            
                            filecounter++;
                            
                            if (i == 0) { //Escolhe o tamanho de partições maior caso as pastas a sincronizar tenham um numero de ficheiros diferentes
                                Packet packk = Packet.fromBytes(frag);
                                size = Math.max(packk.getNPack(), my_files.getPackets().size());
                                ackmax = packk.getNPack();   
                            } 

                            Packet myAck = new Packet(i);
                            p.sendAck(s, ip, port, myAck); 
                            allFragments.getPackets().add(frag);

                            String sRecv1 = new String(frag, StandardCharsets.UTF_8); //Debugging
                            System.out.println("Fiiile: " + sRecv1); //Debugging

                            filereceived = true;
                    

                        }
                        if (frag[0] == 'A'){ //Recebi um Ack
                            System.out.println("Got it Ack"); //Debugging  
                            ackreceived = true;
                            
                            ackcounter++;
                        }

                        //Suposto parar quando já enviou o máximo de acks ou de partições (DOESN'T FUCKING WORK, HELP)
                        if (ackcounter == ackmax && filecounter == size)
                            break;
                        
                    } catch (SocketTimeoutException e) {
                        System.out.println("Retrying");         

                        if (i < my_files.getPackets().size() && !ackreceived) { //Enquanto houver partições para enviar e ainda não ter recebido esse Ack
                            System.out.println("Sending again...");
                            p.send(s, ip, port, my_files, i); 
                        }    
                    }
                    
                }  
                System.out.println("Sai do while"); //Debugging
            }
            
            //Os ficheiros que o outro tem
            byte[] res = allFragments.unifyBytes();
            String sRecv = new String(res, StandardCharsets.UTF_8);
            String[] resultt = sRecv.split("\u001C");
            for(String str: resultt){
                System.out.println("File: " + str);
            }

            //Os ficheiros que me faltam
            String missing_files = FileInfo.missingFiles(folder, sRecv);
            String[] result = missing_files.split("\u001C");
            for(String str: result){
                System.out.println("Missing: " + str);
            }
            


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
