package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;
	
	public WelcomeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		try {


			if (clientMessage.getMessageType() == MessageType.WELCOME) {
				WelcomeMessage welcomeMsg = (WelcomeMessage) clientMessage;

				AppConfig.chordState.init(welcomeMsg);

				String message = initializeUpdateStringMessage();
				AppConfig.timestampedStandardPrint("Sending first update message: " + message);

				UpdateMessage um = new UpdateMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), message);
				MessageUtil.sendMessage(um);

			} else {
				AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
			}

		}catch (Exception e) {
			e.printStackTrace();
		}

	}

//	Message format: "port1, port2, port3 - |n1, n2, n3, n4|"
	private String initializeUpdateStringMessage(){

		String message = "";
		for(int i = 0; i < AppConfig.chordState.CHORD_SIZE; i++)
			message += String.valueOf(AppConfig.chordState.getSuzukiKasamiUtils().getRn().get(i)) + ",";
		message = message.substring(0, message.length() - 1);
		message = "_-|" + message + "|";

		return message;
	}

}
