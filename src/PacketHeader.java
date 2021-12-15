import java.io.*;

public class PacketHeader {
    private char id;
    private int npack;
    private String nome;
    private int seqnum;

    public PacketHeader(int npack, int seqnum) {
        this.id = 'L';
        this.npack = npack;
        this.seqnum = seqnum;
    }

    public PacketHeader(int npack, String nome, int seqnum) {
        this.id = 'F';
        this.npack = npack;
        this.nome = nome;
        this.seqnum = seqnum;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeChar(id);
        dos.writeInt(npack);
        if (id == 'F')
            dos.writeUTF(nome);
        dos.writeInt(seqnum);
        dos.flush();
        dos.close();
        return baos.toByteArray();
    }

    public PacketHeader fromBytes(byte[] b) throws IOException {
        DataInputStream isr = new DataInputStream(new ByteArrayInputStream(b));
        String nome = null;
        char id = isr.readChar();
        int npack = isr.readInt();
        if (id == 'F')
            nome = isr.readUTF();

        int seqnum = isr.readInt();
        isr.close();
        PacketHeader res;

        if (id == 'F')
            res = new PacketHeader(npack, nome, seqnum);
        else res = new PacketHeader(npack, seqnum);

        return res;
    }
}
