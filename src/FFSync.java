import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class FFSync {
    public static void main(String args[]) throws Exception{
        DatagramSocket s;
        FTRapidProtocol p = new FTRapidProtocol();
        int port = 8888;
        String ip = args[0];

        try {
            s = new DatagramSocket(port);
            String message = myFiles();
            byte[] m = message.getBytes();
            SimplePacket pack1 = new SimplePacket(0,0,m); 
            p.send(s, ip, port, pack1);
            System.out.println("sent");
            s.setSoTimeout(10000);

            SimplePacket pack2 = p.receive(s);
            byte[] recv = pack2.getData();
            String sRecv = new String(recv, StandardCharsets.UTF_8);
            System.out.println("Mensagem recebida:\n" + sRecv);
            p.connCheck(s,ip,port,pack1,pack2);

            SimplePacket pack3 = missingFiles(message,sRecv);  
            p.send(s, ip, port, pack3);

            SimplePacket pack4 = p.receive(s);
            pack4.printSimplePacket();

            //Send missing files...
            //Receive missing files...
            
        } catch (Exception e){
            System.err.println(e);
        }    
    }

    public static String myFiles() {
        StringBuilder sb = new StringBuilder();
        File f = new File(System.getProperty("user.dir"));
        for(File file : f.listFiles()) {
            if(file.isFile()) {

                Instant instant = Instant.ofEpochMilli(file.lastModified());
                LocalDateTime data = LocalDateTime.ofInstant(instant,ZoneId.systemDefault());

                sb.append(file.getName() + ";" + data + "\n");
            }
        }
        return sb.toString();
    }

    public static Map<String,FileInfo> parse (String s) throws DateTimeParseException {
        Map<String,FileInfo> res = new HashMap<>();
        List<String> strings = Arrays.asList(s.split("\n"));
        int cont = 0;
        for (String string : strings) {
            if(cont < strings.size()-1) { //Elimina a ultima linha que só tem "lixo"
                String[] info = string.split(";");
                LocalDateTime data = LocalDateTime.parse(info[1]);
                FileInfo hi = new FileInfo(info[0],data);
                res.put(info[0],hi);
            }
            cont++;
        }
        return res;
    }

    public static SimplePacket missingFiles (String msg1, String msg2) {
        System.out.println("first parse");
        Map<String,FileInfo> myInfo = parse(msg1);
        System.out.println("second parse");
        Map<String,FileInfo> otherInfo = parse(msg2);

        List<FileInfo> difFiles = compareFiles(myInfo,otherInfo);

        StringBuilder sb = new StringBuilder();
        System.out.println("Ficheiros diferentes: \n");
        for(FileInfo file : difFiles) {
            System.out.println("Nome: " + file.getName() + "\n");
            sb.append(file.getName() + ";" + file.getData() + "\n");
        }
        String askFiles = sb.toString();
        byte[] msg = askFiles.getBytes();
        SimplePacket pack = new SimplePacket(0,1,msg);  
            
        return pack;
    }

    public static List<FileInfo> compareFiles(Map<String,FileInfo> map1, Map<String,FileInfo> map2) {
        List<FileInfo> files = new ArrayList<>();
        for(Map.Entry entry : map2.entrySet()) {
            if(!map1.containsKey(entry.getKey())) files.add(map2.get(entry.getKey()));
            else {
                LocalDateTime d1 = map1.get(entry.getKey()).getData();
                LocalDateTime d2 = map2.get(entry.getKey()).getData();
                if (d1.isBefore(d2)) files.add(map2.get(entry.getKey()));
            }
        }
        return files;
    }
}
