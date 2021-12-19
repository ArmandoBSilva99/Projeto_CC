import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class FileInfo {
    public static final String file_separator = "\u001C";
    public static final String para_separetor = "\u001F";
    private String name;
    private Instant creation_date;
    private Instant modified_date;

    public FileInfo(String name, Instant creation_date, Instant modified_date) {
        this.name = name;
        this.creation_date = creation_date;
        this.modified_date = modified_date;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreationDate() {
        return this.creation_date;
    }

    public Instant getModifiedDate() {
        return this.modified_date;
    }

    public static Map<String, FileInfo> getDirFileInfo(String filepath) throws IOException {
        File f = new File(filepath);
        BasicFileAttributes attr;
        Map<String, FileInfo> files = new HashMap<>();

        for (File file : Objects.requireNonNull(f.listFiles(File::isFile))) {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileInfo fileInfo = new FileInfo(file.getName(), attr.creationTime().toInstant(), attr.lastModifiedTime().toInstant());
            files.put(file.getName(), fileInfo);
        }

        return files;
    }

    public static Map<String, FileInfo> parse(String s) throws DateTimeParseException {
        Map<String, FileInfo> res = new HashMap<>();
        String[] strings = s.split(FileInfo.file_separator);

        for (String string : strings) {
            String[] info = string.split(FileInfo.para_separetor);
            Instant creation_date = Instant.parse(info[1]);
            Instant modified_date = Instant.parse(info[2]);
            FileInfo hi = new FileInfo(info[0], creation_date, modified_date);
            res.put(info[0], hi);
        }
        return res;
    }

    public static String missingFiles(String local_filepath, String received_file_list) throws IOException {
        Map<String, FileInfo> received_file_map = parse(received_file_list);
        Map<String, FileInfo> local_files = getDirFileInfo(local_filepath);

        List<FileInfo> difFiles = compareFiles(received_file_map, local_files);

        StringBuilder sb = new StringBuilder();

        for (FileInfo file : difFiles)
            sb.append(file.getName() + FileInfo.file_separator);

        return sb.toString();
    }

    private static List<FileInfo> compareFiles(Map<String, FileInfo> local_files, Map<String, FileInfo> received_files) {
        List<FileInfo> files = new ArrayList<>();
        for (Map.Entry entry : received_files.entrySet()) {
            if (!local_files.containsKey(entry.getKey()))
                files.add(received_files.get(entry.getKey()));
            else {
                Instant d1 = local_files.get(entry.getKey()).getModifiedDate();
                Instant d2 = received_files.get(entry.getKey()).getModifiedDate();
                if (d1.isBefore(d2)) files.add(received_files.get(entry.getKey()));
            }
        }
        return files;
    }

    @Override
    public String toString() {
        return this.name + para_separetor +
                this.creation_date.toString() + para_separetor +
                this.modified_date.toString() + file_separator;
    }
}
