package servent.message;

import app.MetaFile;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, Map<String, MetaFile>> values;
	
	public WelcomeMessage(int senderPort, int receiverPort, Map<Integer, Map<String, MetaFile>> values) {
		super(MessageType.WELCOME, senderPort, receiverPort);
		
		this.values = values;
	}
	
	public Map<Integer, Map<String, MetaFile>> getValues() {
		return values;
	}
}
