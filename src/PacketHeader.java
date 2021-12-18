import java.io.*;
import java.nio.charset.StandardCharsets;

public class PacketHeader {
    private char id;
    private int size;
    private int npack;
    private String nome;
    private int seqnum;
    private byte[] data;

    //Temporary
    public PacketHeader(int size, int npack, String nome, int seqnum) {
        this.size = size;
        this.id = 'L';
        this.npack = npack;
        this.seqnum = seqnum;
    }

    //Temporary
    public PacketHeader(int size, int npack, int seqnum) {
        this.size = size;
        this.id = 'L';
        this.npack = npack;
        this.seqnum = seqnum;
        this.data = data;
    }

    public PacketHeader(int size, int npack, int seqnum, byte[] data) {
        this.size = size;
        this.id = 'L';
        this.npack = npack;
        this.seqnum = seqnum;
        this.data = data;
    }

    public PacketHeader(int size, int npack, String nome, int seqnum, byte[] data) {
        this.size = size;
        this.id = 'F';
        this.npack = npack;
        this.nome = nome;
        this.seqnum = seqnum;
        this.data = data;
    }

    public PacketHeader(int seqnum) {
        this.id = 'A';
        this.seqnum = seqnum;
    }

    public byte[] getData(){
        return this.data;
    }


    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(id);
        if (id != 'A'){
            dos.writeInt(size);
            dos.writeInt(npack);
        }    
        if (id == 'F')
            dos.writeUTF(nome);
        dos.writeInt(seqnum);
        dos.flush();
        dos.close();
        return baos.toByteArray();
    }

    public static PacketHeader fromBytes(byte[] b) throws IOException {
        DataInputStream isr = new DataInputStream(new ByteArrayInputStream(b));
        byte[] m = null;
        String nome = null;
        int npack = 0;
        int size = 0;
        char id = (char) isr.readByte();
        if (id != 'A') {
            size = isr.readInt();
            System.out.println("Reading size: " + size);
            m = new byte[size];
            npack = isr.readInt();
        }
        if (id == 'F')
            nome = isr.readUTF();

        int seqnum = isr.readInt();
        isr.read(m, 0, size);
        isr.close();
        String sRecv = new String(b, StandardCharsets.UTF_8);
        System.out.println("Mensagem recebida FROMBYTES B: " + sRecv);
        

        PacketHeader res;
        if (id == 'F')
            res = new PacketHeader(size, npack, nome, seqnum, m);
        if (id == 'L') 
            res = new PacketHeader(size, npack, seqnum, m);
        else
            res = new PacketHeader(seqnum);

        return res;
    }
}
