package servent.message;

import app.MetaFile;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;
	private MetaFile metaFile;

	public TellGetMessage(int senderPort, int receiverPort, int key, MetaFile metaFile) {
		super(MessageType.TELL_GET, senderPort, receiverPort, key + ":" + metaFile.getPath());
		this.metaFile = metaFile;
	}

	public MetaFile getMetaFile() {
		return metaFile;
	}
}
