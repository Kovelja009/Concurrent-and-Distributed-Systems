package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class PutUnlockHandler implements MessageHandler {

    private Message clientMessage;

    public PutUnlockHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.PUT_UNLOCK)
                AppConfig.chordState.getSuzukiKasamiUtils().distributedUnlock();
            else
                AppConfig.timestampedErrorPrint("PutUnlock handler got a message that is not PUT_UNLOCK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
