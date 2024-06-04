package servent.message;

public class DeleteUnlockMessage extends BasicMessage{
    private static final long serialVersionUID = 1338L;
    private int valueDeleted;


    public DeleteUnlockMessage(int senderPort, int receiverPort, int valueDeleted) {
        super(MessageType.DELETE_UNLOCK, senderPort, receiverPort);
        this.valueDeleted = valueDeleted;
    }

    public int getValueDeleted() {
        return valueDeleted;
    }
}
