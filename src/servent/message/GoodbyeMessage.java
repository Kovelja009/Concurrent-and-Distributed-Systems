package servent.message;

public class GoodbyeMessage extends BasicMessage{
    private static final long serialVersionUID = 737L;
    private String goodbyeMessage;

    public GoodbyeMessage(int senderPort, int receiverPort, String goodbyeMessage) {
        super(MessageType.GOODBYE, senderPort, receiverPort, "bye");
        this.goodbyeMessage = goodbyeMessage;
    }

    public String getGoodbyeMessage() {
        return goodbyeMessage;
    }
}
