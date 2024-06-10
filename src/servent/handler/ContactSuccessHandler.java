package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class ContactSuccessHandler implements MessageHandler {

    private Message clientMessage;

    public ContactSuccessHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.CONTACT_SUCCESS) {
                // set successor to alive
                AppConfig.chordState.getNodeKeepAlive().resetSuccessorTimestamp();
                AppConfig.chordState.getNodeKeepAlive().setSuccessorAlive(true);
            } else {
                AppConfig.timestampedErrorPrint("Contact handler got a message that is not CONTACT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
