import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DataPacket {
    public static final int packet_size = 1420;
    private List<byte[]> packets;  //Mudar para List<PacketHeader>

    public DataPacket() {
        packets = new ArrayList<>();
    }

    public List<byte[]> getPackets() {
        return packets;
    }

    //MUDAR CONTAS??
    public void filePackets(String filepath) throws IOException {
        File f = new File(filepath);
        byte[] file = Files.readAllBytes(Paths.get(f.getPath())); // talvez mudar para absolute path!
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

            Packet p = new Packet(min_packet_size, npack, nome, i, data.toByteArray());

            this.packets.add(p.toBytes());
        }
    }

    //MUDAR CONTAS??
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

            Packet fh = new Packet('L', min_packet_size, npack, i, data_packet);
            this.packets.add(fh.toBytes());
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

            Packet fh = new Packet('L', min_packet_size, npack, i, data_packet);
            this.packets.add(fh.toBytes());
        }
    }

    //Mudar
    public byte[] unifyBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] m : this.packets) {
            Packet p = Packet.fromBytes(m);

            baos.write(p.getData());
        }
        byte[] res = baos.toByteArray();
        baos.flush();
        baos.close();
        return res;
    }
}