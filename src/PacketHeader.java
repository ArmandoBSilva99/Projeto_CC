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

    public PacketHeader(int seqnum) {
        this.id = 'A';
        this.seqnum = seqnum;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeChar(id);
        if (id != 'A')
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
        int npack = 0;
        char id = isr.readChar();
        if (id != 'A')
            npack = isr.readInt();
        if (id == 'F')
            nome = isr.readUTF();

        int seqnum = isr.readInt();
        isr.close();
        PacketHeader res;

        if (id == 'F')
            res = new PacketHeader(npack, nome, seqnum);
        if (id == 'L') 
            res = new PacketHeader(npack, seqnum);
        else
            res = new PacketHeader(seqnum);

        return res;
    }
}
