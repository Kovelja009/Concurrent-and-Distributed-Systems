package servent.message;

public class DeleteBackupMessage extends BasicMessage{
    private static final long serialVersionUID = 37L;
    private String path;
    private int key;

    public DeleteBackupMessage(int senderPort, int receiverPort, int key, String path) {
        super(MessageType.DELETE_BACKUP, senderPort, receiverPort, key + ":" + path);
        this.key = key;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public int getKey() {
        return key;
    }
}
