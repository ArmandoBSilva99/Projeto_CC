import java.io.*;


public class Packet {
    public static final char ACK_ID = 'A';
    public static final char FILE_ID = 'F';
    public static final char MISSING_ID = 'M';
    public static final char LIST_ID = 'L';
    public static final char PASS_ID = 'P';
    private char id;
    private int size;
    private int npack;
    private String nome;
    private int seqnum;
    private byte[] data;

    // packet for file
    public Packet(int size, int npack, String nome, int seqnum, byte[] data) {
        this.id = FILE_ID;
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
        this.data = data;
    }

    // packet for ack
    public Packet(int seqnum) {
        this.id = ACK_ID;
        this.seqnum = seqnum;
    }

    public int getSeqNum() {
        return this.seqnum;
    }

    public char getId() {
        return this.id;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getNPack() {
        return this.npack;
    }

    public int getSize() {
        return this.size;
    }

    public String getNome() {
        return this.nome;
    }

    public byte[] toBytes() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.write(id);
        if (id != ACK_ID) {
            dos.writeInt(size);
            dos.writeInt(npack);
        }
        if (id == FILE_ID)
            dos.writeUTF(nome);

        dos.writeInt(seqnum);

        if (id != ACK_ID)
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
        int seqnum = 0;

        char id = (char) isr.readByte();

        if (id != ACK_ID) {
            size = isr.readInt();
            data = new byte[size];
            npack = isr.readInt();
        }

        if (id == FILE_ID)
            nome = isr.readUTF();

        seqnum = isr.readInt();

        if (id != ACK_ID && size != 0)
            isr.read(data, 0, size);

        isr.close();

        Packet res;
        if (id == FILE_ID)
            res = new Packet(size, npack, nome, seqnum, data);
        else if (id == ACK_ID)
            res = new Packet(seqnum);
        else
            res = new Packet(id, size, npack, seqnum, data);

        return res;
    }
}
