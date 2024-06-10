package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class PongHandler implements MessageHandler {

    private Message clientMessage;

    public PongHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            // reset counter, set status to 0, broadcast to false
            if (clientMessage.getMessageType() == MessageType.PONG) {
                AppConfig.chordState.getNodeKeepAlive().setDoneBroadcast(false);
                AppConfig.chordState.getNodeKeepAlive().resetTimestamp();
                AppConfig.chordState.getNodeKeepAlive().setStatus(0);


            } else {
                AppConfig.timestampedErrorPrint("Pong handler got a message that is not PONG");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
