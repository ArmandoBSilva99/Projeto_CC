import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

public class ProcessPacket implements Runnable {
    private String filepath;
    private byte[] packet;
    private ThreadPool threadPool;
    private PacketManager packetManager;
    private InetAddress address;
    private int port;

    public ProcessPacket(String filepath, byte[] packet, ThreadPool threadPool, PacketManager p, InetAddress address, int port) {
        this.filepath = filepath;
        this.packet = packet;
        this.threadPool = threadPool;
        this.packetManager = p;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Packet received = Packet.fromBytes(this.packet);
            packetManager.addPacket(port, received);
            DatagramSocket to_send = packetManager.getSocket(port);
            byte[] ack = new Packet(received.getSeqNum()).toBytes();
            DatagramPacket datagramAck = new DatagramPacket(ack, ack.length, this.address, this.port);
            to_send.send(datagramAck);

            if (received.getSeqNum() == received.getNPack() - 1) {
                DataPackets to_process = packetManager.removeDataPacket(port);
                packetManager.closeSocket(port);
                if (to_process != null) {
                    byte[] data = to_process.unifyBytes();
                    if (received.getId() == Packet.FILE_ID) {
                        writeFile(received.getNome(), data);
                    } else if (received.getId() == Packet.LIST_ID) {
                        String his_file_list = new String(data, StandardCharsets.UTF_8);

                        DataPackets missing_files = new DataPackets();

                        missing_files.missingFileListPackets(filepath, his_file_list);
                        packetManager.makeFileInfoMap(his_file_list);

                        System.out.println("---------TO SEND---------");
                        if (missing_files.getPackets().get(0).getData().length != 0) {
                            for (String s : FileInfo.missingFiles(filepath, his_file_list).split(FileInfo.FILE_SEPARATOR)) {
                                System.out.println(" " + s);
                            }
                        } else {
                            System.out.println(" All up to date");
                        }
                        System.out.println("-------------------------");

                        SendPackets for_thread = new SendPackets(missing_files, address, FFSync.MAIN_PORT);
                        threadPool.execute(for_thread);
                    } else if (received.getId() == Packet.MISSING_ID && data.length != 0) {
                        String missing_files = new String(data, StandardCharsets.UTF_8);
                        for (String filename : missing_files.split(FileInfo.FILE_SEPARATOR)) {
                            String combined_filepath = filepath + "/" + filename;
                            SendPackets for_thread = new SendPackets(combined_filepath, address, FFSync.MAIN_PORT);
                            threadPool.execute(for_thread);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void writeFile(String filename, byte[] file_data) throws IOException {
        String combined_filepath = filepath + "/" + filename;
        File f = new File(combined_filepath);
        f.createNewFile();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(file_data);
        fos.flush();
        fos.close();
        FileInfo fi = packetManager.getFileInfoMap().get(filename);

        // talvez setCreationDate
        Files.setLastModifiedTime(f.toPath(), FileTime.from(fi.getModifiedDate()));
    }
}
