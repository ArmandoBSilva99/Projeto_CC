import java.io.*;


public class Packet {
    private char id;
    private int size;
    private int npack;
    private String nome;
    private int seqnum;
    private byte[] data;


    public Packet(char id){
        this.id = id;
    }
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

    // packet for ack
    public Packet(int seqnum) {
        this.id = 'A';
        this.seqnum = seqnum;
    }

    public int getSeqNum(){
        return this.seqnum;
    }

    public char getId(){
        return this.id;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getNPack() {
        return this.npack;
    }

    public int getSize(){
        return this.size;
    }

    public String getNome(){
        return this.nome;
    }

    public byte[] toBytes() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.write(id);
        if (id != 'A' && id != 'M') {
            dos.writeInt(size);
            dos.writeInt(npack);
        }
        if (id == 'F')
            dos.writeUTF(nome);

        if (id != 'M')
            dos.writeInt(seqnum);

        if (id != 'A' && id != 'M')
            dos.write(data);

        dos.flush();
        dos.close();
        baos.flush();
        //System.out.println("To bytes completed");
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
        
        if (id != 'A' && id != 'M') {
            size = isr.readInt();
            data = new byte[size];
            npack = isr.readInt();
        }
        
        if (id == 'F')
            nome = isr.readUTF();
        
        if (id != 'M')
            seqnum = isr.readInt();

        if (id != 'A' && size != 0 && id != 'M')
            isr.read(data, 0, size);
        
        isr.close();

        Packet res;
        if (id == 'M'){
            res = new Packet(id);
            //System.out.println("Meu ID: " + res.getId());
        }    
        
        else if (id == 'F')
            res = new Packet(size, npack, nome, seqnum, data);
        else if (id == 'A')
            res = new Packet(seqnum);
        else
            res = new Packet(id, size, npack, seqnum, data);

        //System.out.println("Apos os IDs");

        if(id == 'M') {

        System.out.println("ID: " + res.getId());
        //System.out.println("Size: " + res.getSize());
        //System.out.println("NPack: " + res.getNPack());
        //System.out.println("Seqnum: " + res.getSeqNum());
        System.out.println("Data: " + res.getData());

        }

        return res;
    }
}
