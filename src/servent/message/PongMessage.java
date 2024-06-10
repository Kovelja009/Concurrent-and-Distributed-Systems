package servent.message;

public class PongMessage extends BasicMessage {
    private static final long serialVersionUID = 43425L;

    public PongMessage(int senderPort, int receiverPort) {
        super(MessageType.PONG, senderPort, receiverPort, "pong");
    }
}
