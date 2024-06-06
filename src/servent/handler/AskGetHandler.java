package servent.handler;

import java.util.Map;

import app.AppConfig;
import app.MetaFile;
import app.ServentInfo;
import servent.message.AskGetMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;

public class AskGetHandler implements MessageHandler {

	private Message clientMessage;
	
	public AskGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_GET) {
			try {
				AskGetMessage askGetMessage = (AskGetMessage) clientMessage;

				int key = askGetMessage.getKey();
				String path = askGetMessage.getPath();

				if (AppConfig.chordState.isKeyMine(key)) { // if I should have this key
					MetaFile resultFile;
					MetaFile notFound = new MetaFile("not_found", -1, false);

					// we have the hashmap
					if(AppConfig.chordState.getValueMap().containsKey(key))
						resultFile = AppConfig.chordState.getValueMap().get(key).getOrDefault(path, notFound);
					else
						resultFile = notFound;

					TellGetMessage tgm = new TellGetMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort(),
															key, resultFile);
					MessageUtil.sendMessage(tgm);
				} else { // If I don't have this key, then propagate the request through the network
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
					AskGetMessage agm = new AskGetMessage(clientMessage.getSenderPort(), nextNode.getListenerPort(), key, path);
					MessageUtil.sendMessage(agm);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

}