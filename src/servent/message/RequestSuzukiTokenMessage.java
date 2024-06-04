package servent.message;

public class RequestSuzukiTokenMessage extends BasicMessage{

    private static final long serialVersionUID = 3899837287772127636L;
    private int senderRN;

    public RequestSuzukiTokenMessage(int senderPort, int receiverPort, int senderRN) {
        super(MessageType.REQUEST_SUZUKI_TOKEN, senderPort, receiverPort);
        this.senderRN = senderRN;
    }

    public int getSenderRN() {
        return senderRN;
    }
}
