package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PutMessage;

public class PutHandler implements MessageHandler {

	private Message clientMessage;
	
	public PutHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override

	// TODO: adopt so that we can send String path to the file
	public void run() {
		try {
			if (clientMessage.getMessageType() == MessageType.PUT) {
				PutMessage putMessage = (PutMessage) clientMessage;

				int key = putMessage.getKey();
				String value = putMessage.getPath();
				int originalPort = putMessage.getOriginalSenderPort();
				boolean isPublic = putMessage.isPublic();

				AppConfig.chordState.putValue(key, value, originalPort, isPublic);
			} else {
				AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}
