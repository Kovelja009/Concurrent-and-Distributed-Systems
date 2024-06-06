package servent.message;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;
	private int originalSenderPort;
	private String path;
	private int key;
	private boolean isPublic;

	public PutMessage(int senderPort, int receiverPort, int key, int originalSenderPort, String value, boolean isPublic) {
		super(MessageType.PUT, senderPort, receiverPort, key + ":" + value);
		this.originalSenderPort = originalSenderPort;
		this.key = key;
		this.path = value;
		this.isPublic = isPublic;
	}

	public int getOriginalSenderPort() {
		return originalSenderPort;
	}

	public String getPath() {
		return path;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public int getKey() {
		return key;
	}
}
