import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DataPacket {
    public static final int packet_size = 1420;
    private List<Packet> packets;  //Mudar para List<PacketHeader>

    public DataPacket() {
        packets = new ArrayList<>();
    }

    public List<Packet> getPackets() {
        return packets;
    }

    public void filePackets(String filepath) throws IOException {
        File f = new File(filepath);
        byte[] file = Files.readAllBytes(Paths.get(filepath).toAbsolutePath()); // talvez mudar para absolute path!
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        int file_size = (int) f.length();
        String nome = f.getName();
        int len_header = (nome.length() + 2) + 13;                   // 2 -> cabeçalho da writeUTF da classe DataInputStream e 13 -> 1 do id,4 do size, 4 do npack, 4 do seqnum
        int data_packet_size = packet_size - len_header;
        int npack = 1 + (file_size / data_packet_size);
        int min_packet_size;

        for (int i = 0; i < npack; i++) {
            data.reset();
            min_packet_size = Math.min((file_size - i * data_packet_size), data_packet_size);
            data.write(file, i * data_packet_size, min_packet_size);

            if (data.toByteArray().length == 0) npack = 0;

            Packet p = new Packet(min_packet_size, npack, nome, i, data.toByteArray());

            this.packets.add(p);
        }
    }

    public void fileListPackets(String filepath) throws IOException, NullPointerException {
        StringBuilder sb = new StringBuilder();

        Map<String, FileInfo> files = FileInfo.getDirFileInfo(filepath);
    
        files.values().forEach(fileInfo -> sb.append(fileInfo.toString())); // talvez funcione testar ??

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte[] list = sb.toString().getBytes(StandardCharsets.UTF_8);

        int list_size = list.length;
        int len_header = 13;
        int data_packet_size = packet_size - len_header;
        int npack = 1 + (list_size / data_packet_size);
        int min_packet_size;

        for (int i = 0; i < npack; i++) {
            data.reset();
            min_packet_size = Math.min((list_size - i * data_packet_size), data_packet_size);
            data.write(list, i * data_packet_size, min_packet_size);
            byte[] data_packet = data.toByteArray();
            if (data.toByteArray().length == 0) npack = 0;

            Packet fh = new Packet(Packet.LIST_ID, min_packet_size, npack, i, data_packet);
            this.packets.add(fh);
        }
    }

    public void missingFileListPackets(String local_filepath, String received_file_list) throws IOException {
        byte[] missing_files = FileInfo.missingFiles(local_filepath, received_file_list).getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream data = new ByteArrayOutputStream();

        int list_size = missing_files.length;
        int len_header = 13;
        int data_packet_size = packet_size - len_header;
        int npack = 1 + (list_size / data_packet_size);
        int min_packet_size;

        for (int i = 0; i < npack; i++) {
            data.reset();

            min_packet_size = Math.min((list_size - i * data_packet_size), data_packet_size);
            data.write(missing_files, i * data_packet_size, min_packet_size);
            byte[] data_packet = data.toByteArray();
            if (data.toByteArray().length == 0) npack = 0;
            Packet fh = new Packet(Packet.LIST_ID, min_packet_size, npack, i, data_packet);
            this.packets.add(fh);
        }
    }

    //Mudar
    public byte[] unifyBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (Packet p : this.packets) {
            //System.out.println("After fromBytes");

            if(p.getData() != null)
                baos.write(p.getData());
            
            //System.out.println("after write");
        }

        byte[] res = baos.toByteArray();

        baos.flush();
        baos.close();

        return res;
    }


    public void dataPacketToFile(String filepath) throws IOException{
        //Packet name = Packet.fromBytes(this.packets.get(0));
        //System.out.println("Before unified");
        byte[] unified = this.unifyBytes();
        //System.out.println("After unified");
        try {
            
            Packet ps = this.packets.get(0);
            if (ps.getNome() != null) {
                File outputFile = new File(filepath + "/" + ps.getNome());
                outputFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(unified);
                fos.flush();
                fos.close();
            }
        }
        catch (Exception e){

        }    
    }
}