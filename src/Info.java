import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Info {
    int port;
    String adress;
    String folder;
    String peer;
    File[] files;

    public Info(int port, String adress, String folder, String peer) {
        this.port = port;
        this.adress = adress;this.folder = folder;
        this.peer = peer;
        File f = new File(System.getProperty("user.dir") + "/" + folder);
        this.files = f.listFiles();
    }

    void printFiles() {
        StringBuilder sb = new StringBuilder();
        for(File file : this.files) {
            if(file.isFile()) {
                System.out.println(file.getName());
                sb.append("/" + file.getName()+"\n");
            }
        }
    }
}
