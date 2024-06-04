package servent;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.DeleteUnlockMessage;
import servent.message.Message;
import servent.message.MessageType;

public class DeleteUnlockHandler implements MessageHandler {
    private Message clientMessage;

    public DeleteUnlockHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.DELETE_UNLOCK) {
                AppConfig.chordState.getSuzukiKasamiUtils().distributedUnlock();

                int valueDeleted = ((DeleteUnlockMessage) clientMessage).getValueDeleted();
                AppConfig.timestampedStandardPrint("DELETE_UNLOCK: " + valueDeleted);
            } else {
                AppConfig.timestampedErrorPrint("DeleteUnlock handler got a message that is not DELETE_UNLOCK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
