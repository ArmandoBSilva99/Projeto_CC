import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class PacketManager {
    private ReentrantLock packets_lock;
    private ReentrantLock socket_lock;
    private Map<Integer, DataPackets> packets;
    private Map<Integer, DatagramSocket> open_sockets;
    private Map<String, FileInfo> file_info_map;

    public PacketManager() {
        this.packets_lock = new ReentrantLock();
        this.socket_lock = new ReentrantLock();
        this.packets = new HashMap<>();
        this.open_sockets = new HashMap<>();
        this.file_info_map = new HashMap<>();
    }

    public void addPacket(int port, Packet packet) throws SocketException {
        socket_lock.lock();
        this.open_sockets.putIfAbsent(port, new DatagramSocket());
        socket_lock.unlock();

        packets_lock.lock();
        packets.putIfAbsent(port, new DataPackets());
        packets.get(port).add(packet);
        packets_lock.unlock();
    }

    public Map<String, FileInfo> getFileInfoMap() {
        return file_info_map;
    }

    public void makeFileInfoMap(String file_list) {
        this.file_info_map = FileInfo.parse(file_list);
    }

    public DatagramSocket getSocket(int port) {
        return this.open_sockets.get(port);
    }

    public void closeSocket(int port) {
        socket_lock.lock();
        if (this.open_sockets.containsKey(port)) {
            DatagramSocket s = this.open_sockets.remove(port);
            s.close();
        }
        socket_lock.unlock();
    }

    public DataPackets removeDataPacket(int port) {
        packets_lock.lock();
        DataPackets res = null;
        if (this.packets.containsKey(port))
            res = this.packets.remove(port);
        packets_lock.unlock();
        return res;
    }
}
