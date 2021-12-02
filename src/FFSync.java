public class FFSync {
    public static void main(String[] args) {
        FTRapidProtocol protocol = new FTRapidProtocol();
        Info info = new Info(80, "localhost", args[0], args[1]);
        info.printFiles();
    }
}
