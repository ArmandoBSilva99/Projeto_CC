import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SimplePacket {
    private int ack;
    private int msgType;
    private byte[] data = {};

    SimplePacket(){}

    SimplePacket(int ack) {
        this.ack = ack;
    }

    SimplePacket(int ack, int msgType, byte[] data) {
        this.ack = ack;
        this.msgType = msgType;
        this.data = data;
    }

    void setData(byte[] data) {
        this.data = data;
    }

    void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    void setAck(int ack) {
        this.ack = ack;
    }

    int getAck(){
        return this.ack;
    }
    int getMsgType() {
        return this.msgType;
    }

    byte[] getData(){
        return this.data;
    }

    public byte[] packetToBytes() throws IOException {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeInt(this.ack);
        dataOut.write(this.data);
        dataOut.close();
        dataOut.flush();
        return byteOut.toByteArray();
    }
    
    public void bytesToPacket(byte[] headerBytes) throws IOException {
        final ByteArrayInputStream byteIn = new ByteArrayInputStream(headerBytes);
        final DataInputStream dataIn = new DataInputStream(byteIn);
        this.ack = dataIn.readInt();
        this.data = dataIn.readAllBytes();

        dataIn.close();
        byteIn.close();
    }

    public void printSimplePacket() throws IOException{
        byte[] buf = packetToBytes();
        String resp = new String(buf, 0, buf.length);
        System.out.println(resp);
    }
}
