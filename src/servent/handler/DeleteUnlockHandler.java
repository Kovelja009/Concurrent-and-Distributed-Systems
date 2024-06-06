package servent.handler;

import app.AppConfig;
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

                DeleteUnlockMessage dum = (DeleteUnlockMessage) clientMessage;
                int wasSuccessful = dum.getResult();
                String path = dum.getPath();

                if(wasSuccessful == 0)
                    AppConfig.timestampedStandardPrint("DELETE_UNLOCK: " + path + " was successfully deleted.");
                else if (wasSuccessful == -1)
                    AppConfig.timestampedStandardPrint("DELETE_UNLOCK: " + path + " not deleted, we are not the owner.");
                else
                    AppConfig.timestampedStandardPrint("DELETE_UNLOCK: " + path + " not deleted, file not found.");
            } else {
                AppConfig.timestampedErrorPrint("DeleteUnlock handler got a message that is not DELETE_UNLOCK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
