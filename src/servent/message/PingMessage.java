package servent.message;

public class PingMessage extends BasicMessage {
    private static final long serialVersionUID = 4342L;

    public PingMessage(int senderPort, int receiverPort) {
        super(MessageType.PING, senderPort, receiverPort, "ping");
    }
}
