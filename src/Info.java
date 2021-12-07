import java.io.File;

public class Info {
    int port;
    String address;
    String folder;
    String peer;
    File[] files;

    public Info(int port, String address, String folder, String peer) {
        this.port = port;
        this.address = address;
        this.folder = folder;
        this.peer = peer;
        File f = new File(System.getProperty("user.dir") + "/" + folder);
        this.files = f.listFiles();
    }

    StringBuilder printFiles() {
        StringBuilder sb = new StringBuilder();
        for(File file : this.files) {
            if(file.isFile()) {
                System.out.println(file.getName());
                sb.append("/" + file.getName()+"\n");
            }
        }
        return sb;
    }
}
