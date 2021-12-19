import java.io.*;
import java.nio.charset.StandardCharsets;
import java.lang.Object;

public class Packet {
    private char id;
    private int size;
    private int npack;
    private String nome;
    private int seqnum;
    private byte[] data;

    // packet for file
    public Packet(int size, int npack, String nome, int seqnum, byte[] data) {
        this.id = 'F';
        this.size = size;
        this.nome = nome;
        this.npack = npack;
        this.seqnum = seqnum;
        this.data = data.clone();
    }

    // packet for file list
    public Packet(char id, int size, int npack, int seqnum, byte[] data) {
        this.id = id;
        this.size = size;
        this.npack = npack;
        this.seqnum = seqnum;
        this.data = data.clone();
    }

    public Packet(int seqnum) {
        this.id = 'A';
        this.seqnum = seqnum;
    }

    public byte[] getData() {
        return this.data;
    }


    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.write(id);
        if (id != 'A') {
            dos.writeInt(size);
            dos.writeInt(npack);
        }
        if (id == 'F')
            dos.writeUTF(nome);
        dos.writeInt(seqnum);
        dos.write(data);

        dos.flush();
        dos.close();
        baos.flush();
        return baos.toByteArray();
    }

    public static Packet fromBytes(byte[] b) throws IOException {
        DataInputStream isr = new DataInputStream(new ByteArrayInputStream(b));
        byte[] data = null;
        String nome = null;
        int npack = 0;
        int size = 0;
        char id = (char) isr.readByte();
        if (id != 'A') {
            size = isr.readInt();
            System.out.println("Reading size: " + size);
            data = new byte[size];
            npack = isr.readInt();
        }
        if (id == 'F')
            nome = isr.readUTF();

        int seqnum = isr.readInt();
        isr.read(data, 0, size);
        isr.close();
        String sRecv = new String(b, StandardCharsets.UTF_8);
        System.out.println("Mensagem recebida FROMBYTES B: " + sRecv);


        Packet res;
        if (id == 'F')
            res = new Packet(size, npack, nome, seqnum, data);
        if (id == 'A')
            res = new Packet(seqnum);
        else
            res = new Packet(id, size, npack, seqnum, data);

        return res;
    }
}
