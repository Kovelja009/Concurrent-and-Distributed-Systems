package servent.handler;

import app.AppConfig;
import app.suzuki_kasami.SuzukiKasamiToken;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SendSuzukiTokenMessage;
import servent.message.util.MessageUtil;

import java.io.PrintWriter;
import java.net.Socket;

public class SorryHandler implements MessageHandler {

	private Message clientMessage;
	
	public SorryHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	// should send a message to the bootstrap server
	// also send token to the first node in queue or if it is empty then send token to the node that send us sry message
	@Override
	public void run() {
		try {


			if (clientMessage.getMessageType() == MessageType.SORRY) {
				AppConfig.timestampedStandardPrint("Couldn't enter Chord system because of collision. Change my listener port, please.");

				// try to send token to the first thing in queue
				AppConfig.chordState.getSuzukiKasamiUtils().possibleSendingToken();

				SuzukiKasamiToken token = AppConfig.chordState.getSuzukiKasamiUtils().getToken();
				// if noone was asking for token, then just send it to the node that sent us SORRY
				if (token != null) {
					int senderPort = clientMessage.getSenderPort();
					Message tokenMessage = new SendSuzukiTokenMessage(AppConfig.myServentInfo.getListenerPort(), senderPort, token);
					AppConfig.timestampedStandardPrint("Sending Suzuki-Kasami token to " + senderPort);
					MessageUtil.sendMessage(tokenMessage);
				}

				// send message to bootstrap server
				AppConfig.timestampedStandardPrint("Sending sorry message to bootstrap server.");
				sorryBootstrap();

				System.exit(0);
			} else {
				AppConfig.timestampedErrorPrint("Sorry handler got a message that is not SORRY");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sorryBootstrap() {
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Sorry\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			bsSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			AppConfig.timestampedErrorPrint("SorryBootstrap failed.");
		}
	}

}
