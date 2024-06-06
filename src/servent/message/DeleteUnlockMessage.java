package servent.message;

public class DeleteUnlockMessage extends BasicMessage{
    private static final long serialVersionUID = 1338L;
    private String path;
    private int result;


    public DeleteUnlockMessage(int senderPort, int receiverPort, String path, int result) {
        super(MessageType.DELETE_UNLOCK, senderPort, receiverPort);
        this.path = path;
        this.result = result;
    }

    public String getPath() {
        return path;
    }

    public int getResult() {
        return result;
    }
}
