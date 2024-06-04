package servent.message;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;
	private int originalSenderPort;


	public PutMessage(int senderPort, int receiverPort, int key, int value, int originalSenderPort) {
		super(MessageType.PUT, senderPort, receiverPort, key + ":" + value);
		this.originalSenderPort = originalSenderPort;
	}

	public int getOriginalSenderPort() {
		return originalSenderPort;
	}
}
