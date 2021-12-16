import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DataPacket {
    private static final int packet_size = 1420;
    private List<byte[]> packets;

    public DataPacket() {
        packets = new ArrayList<>();
    }

    public List<byte[]> getPackets() {
        return packets;
    }

    public void filePackets(String filepath) throws IOException {
        File f = new File(filepath);
        byte[] file = Files.readAllBytes(Paths.get(f.getPath())); // talvez mudar para absolute path!
        ByteArrayOutputStream aux = new ByteArrayOutputStream();

        int file_size = (int) f.length();
        String nome = f.getName();
        int len_header = (nome.length() + 2) + 9;                   // 2 -> cabeçalho da writeUTF da classe DataInputStream e 9 -> 1 do id, 4 do npack, 4 do seqnum
        int data_packet_size = packet_size - len_header;
        int npack = 1 + (file_size / data_packet_size);

        for (int i = 0; i < npack; i++) {
            PacketHeader ph = new PacketHeader(npack, nome, i);
            aux.write(ph.toBytes());
            aux.write(file, i * data_packet_size, Math.min((file_size - i * data_packet_size), data_packet_size));
            this.packets.add(aux.toByteArray());
        }
    }

    public void fileListPackets(String filepath) throws IOException {
        File f = new File(filepath);
        File[] f_list = f.listFiles();
        BasicFileAttributes attr;

        StringBuilder sb = new StringBuilder();
        for (File file : f_list) {
            if (file.isFile()) {
                attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                sb.append(file.getName() + ";" + attr.creationTime().toString() + ";" + attr.lastModifiedTime().toInstant().toString() + ";");
            }
        }

        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        byte[] list = sb.toString().getBytes(StandardCharsets.UTF_8);

        int list_size = list.length;
        int len_header = 9;
        int data_packet_size = packet_size - len_header;
        int npack = 1 + (list_size / data_packet_size);

        for (int i = 0; i < npack; i++) {
            PacketHeader fh = new PacketHeader(npack, i);
            aux.write(fh.toBytes());
            aux.write(list, i * data_packet_size, Math.min((list_size - i * data_packet_size), data_packet_size));
            this.packets.add(aux.toByteArray());
        }
    }
}