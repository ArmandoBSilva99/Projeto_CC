import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class FTRapidProtocol implements Runnable {
    private static final int MIN_THREADS = 15;
    private static final int MAX_TASKS_WAIT = 60;

    public String filepath;
    private InetAddress ip;

    private PacketManager packetManager;
    private ThreadPool threadPool;

    public FTRapidProtocol(String filepath, InetAddress ip) {
        this.filepath = filepath;
        this.ip = ip;
        this.packetManager = new PacketManager();
        this.threadPool = new ThreadPool(MIN_THREADS, MAX_TASKS_WAIT);
    }

    @Override
    public void run() {
        try {
            DatagramSocket main_socket = new DatagramSocket(FFSync.MAIN_PORT);

            Packet packConn = new Packet(0);
            if (connCheck(main_socket, ip, FFSync.MAIN_PORT, packConn) == -1)
                return; //Começa a conexão, i.e, verifica se estão comunicáveis

            DataPackets fileList = new DataPackets();

            fileList.fileListPackets(filepath);
            if (fileList.getPackets().get(0).getData().length != 0) {
                SendPackets my_file_list = new SendPackets(fileList, ip, FFSync.MAIN_PORT);
                threadPool.execute(my_file_list);
            }

            while (true) {
                main_socket.setSoTimeout(10000);
                byte[] received_data = new byte[DataPackets.PACKET_SIZE];
                DatagramPacket received_packet = new DatagramPacket(received_data, DataPackets.PACKET_SIZE);

                main_socket.receive(received_packet);
                ProcessPacket processPacket = new ProcessPacket(filepath, received_packet.getData(), threadPool, packetManager, ip, received_packet.getPort());
                threadPool.execute(processPacket);
            }
        } catch (SocketTimeoutException e) {
            threadPool.waitUntilAllTasksFinished();
            //System.out.println("----------------------");
            //System.out.println("All Done!");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int connCheck(DatagramSocket s, InetAddress hostS, int port, Packet p) throws IOException {
        for (int i = 1; i < 5; i++)
            if (safetyMeasure(s, hostS, port)) {
                System.out.println("Connection Established\n");
                return 0;
            } else {
                System.out.println("Wrong shared password... Try Again (" + i + "/3)");
            }
        System.out.println("Ran out of tries");
        return -1;
    }

    private boolean safetyMeasure(DatagramSocket s, InetAddress hostS, int port) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        //boolean sharedPassword = false;
        System.out.println("Insert your Shared Password: ");
        String pass = reader.readLine();
        byte[] m = pass.getBytes();
        Packet p = new Packet(Packet.PASS_ID, m.length, 1, 0, m);
        for (int i = 0; i < 3; ) {
            s.setSoTimeout(3000);
            sendAck(s, hostS, port, p);
            try {
                byte[] rs = receive(s);
                Packet received = Packet.fromBytes(rs);
                if (received.getId() == Packet.PASS_ID) {
                    String their_pass = new String(received.getData(), StandardCharsets.UTF_8);
                    if (pass.equals(their_pass)) return true;
                    else return false;
                }
            } catch (SocketTimeoutException e) {
                sendAck(s, hostS, port, p);
            }
        }
        return false;
    }

    //Receive Packets
    public byte[] receive(DatagramSocket s) throws IOException {
        byte[] packetBytes = new byte[DataPackets.PACKET_SIZE];
        DatagramPacket dp = new DatagramPacket(packetBytes, packetBytes.length);
        s.receive(dp);
        return packetBytes;
    }

    public void sendAck(DatagramSocket s, InetAddress hostS, int port, Packet p) throws IOException {
        byte[] m = p.toBytes();
        s.send(new DatagramPacket(m, m.length, hostS, port));
    }

}
