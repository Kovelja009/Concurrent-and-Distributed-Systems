package servent.message;

public class DeleteMessage extends BasicMessage {
    private static final long serialVersionUID = 4212L;
    private int originalSenderPort;

    public DeleteMessage(int senderPort, int receiverPort, int key, int value, int originalSenderPort) {
        super(MessageType.DELETE, senderPort, receiverPort, key + ":" + value);
        this.originalSenderPort = originalSenderPort;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }
}
