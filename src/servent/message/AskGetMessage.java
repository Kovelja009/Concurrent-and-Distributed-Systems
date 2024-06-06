package servent.message;

public class AskGetMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;
	private int key;
	private String path;

	public AskGetMessage(int senderPort, int receiverPort, int key, String path) {
		super(MessageType.ASK_GET, senderPort, receiverPort, key + ":" + path);
		this.key = key;
		this.path = path;
	}

	public int getKey() {
		return key;
	}

	public String getPath() {
		return path;
	}
}
