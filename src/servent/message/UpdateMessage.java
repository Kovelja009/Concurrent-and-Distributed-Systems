package servent.message;

import app.MetaFile;

import java.util.HashMap;
import java.util.Map;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;
	private Map<Integer, Map<String, MetaFile>> files;

	public UpdateMessage(int senderPort, int receiverPort, String text, Map<Integer, Map<String, MetaFile>> files) {
		super(MessageType.UPDATE, senderPort, receiverPort, text);
		this.files = files;
	}

	public Map<Integer, Map<String, MetaFile>> getFiles() {
		return files;
	}
}
