package servent.handler;

import app.AppConfig;
import app.MetaFile;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;

public class TellGetHandler implements MessageHandler {

	private Message clientMessage;
	
	public TellGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		try {


			if (clientMessage.getMessageType() == MessageType.TELL_GET) {
				// distributed unlock
				AppConfig.chordState.getSuzukiKasamiUtils().distributedUnlock();

				MetaFile metaFile = ((TellGetMessage) clientMessage).getMetaFile();

				// file not found
				if(metaFile.getOwnerPort() == -1)
					AppConfig.timestampedStandardPrint("File with path {" + metaFile.getPath() + "} not found!");
				else{
					if(AppConfig.chordState.canRead(metaFile))
						AppConfig.timestampedStandardPrint("File {" + metaFile.getPath() + "} CONTENT -> " + AppConfig.readTextFile(metaFile.getPath()));
					else
						AppConfig.timestampedStandardPrint("Don't have permission to read file: " + metaFile.getPath());
				}
			} else {
				AppConfig.timestampedErrorPrint("Tell get handler got a message that is not TELL_GET");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
