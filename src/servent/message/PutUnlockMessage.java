package servent.message;

public class PutUnlockMessage extends BasicMessage {
    private static final long serialVersionUID = 1337L;

    public PutUnlockMessage(int senderPort, int receiverPort) {
        super(MessageType.PUT_UNLOCK, senderPort, receiverPort);
    }
}
