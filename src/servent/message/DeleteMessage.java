package servent.message;

public class DeleteMessage extends BasicMessage {
    private static final long serialVersionUID = 4212L;
    private int originalSenderPort;
    private int key;
    private String path;


    public DeleteMessage(int senderPort, int receiverPort, int key, String value, int originalSenderPort) {
        super(MessageType.DELETE, senderPort, receiverPort, key + ":" + value);
        this.originalSenderPort = originalSenderPort;
        this.key = key;
        this.path = value;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }

    public int getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }
}
