import java.net.DatagramSocket;

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
            
            String allFragments = p.sendListOfPackages(s, ip, port, my_files);
        
            
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
            miss.missingFileListPackets(folder, allFragments);
            String receivedPackages = p.sendListOfPackages(s, ip, port, miss);
            
            
            

                
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
