import java.net.DatagramSocket;
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
            if (p.connCheck(s, ip, port, packConn) == -1) return; //Começa a conexão, i.e, verifica se estão comunicáveis 
            DataPacket my_files = new DataPacket();
            my_files.fileListPackets(folder); //List of files in folder
            
            DataPacket allFragments = p.sendListOfPackages(s, ip, port, my_files);
            //System.out.println("All fragments done: ");
            byte[] res = allFragments.unifyBytes();

            String allFragmentsStr = new String(res, StandardCharsets.UTF_8);
            
            //Os ficheiros que me faltam
            String missing_files = FileInfo.missingFiles(folder, allFragmentsStr);
            if (missing_files.length() != 0) {
                String[] result = missing_files.split("\u001C");
                for(String str: result){
                    System.out.println("Missing files to send: " + str);
                }
            }    
            else {
                System.out.println("No files needed to send");
            }
            
            DataPacket miss = new DataPacket();
            miss.missingFileListPackets(folder, allFragmentsStr);

            DataPacket receivedPackages = p.sendListOfPackages(s, ip, port, miss);
            byte[] res1 = receivedPackages.unifyBytes();

            String result = new String(res1, StandardCharsets.UTF_8);
             

            //System.out.println("Going in"); 
            int size = allFragmentsStr.split(FileInfo.file_separator).length;
            //System.out.println("Size: " + size);
            p.sendNReceiveFiles(s, ip, port, missing_files, folder, size);
            
            

                
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
