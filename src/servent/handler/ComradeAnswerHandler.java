package servent.handler;

import app.AppConfig;
import servent.message.ComradeAnswerMessage;
import servent.message.Message;
import servent.message.MessageType;

public class ComradeAnswerHandler implements MessageHandler {
    private Message clientMessage;

    public ComradeAnswerHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if(clientMessage.getMessageType() == MessageType.COMRADE_ANSWER) {
                ComradeAnswerMessage cam = (ComradeAnswerMessage) clientMessage;
                // reset state to 0, broadcast to false, reset timeStamp
                AppConfig.chordState.getNodeKeepAlive().setStatus(0);
                AppConfig.chordState.getNodeKeepAlive().setDoneBroadcast(false);
                AppConfig.chordState.getNodeKeepAlive().resetTimestamp();
            } else {
                AppConfig.timestampedErrorPrint("ComradeAnswer handler got a message that is not COMRADE_ANSWER");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
