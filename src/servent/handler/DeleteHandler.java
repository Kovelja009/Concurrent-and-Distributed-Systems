package servent.handler;

import app.AppConfig;
import servent.message.DeleteMessage;
import servent.message.Message;
import servent.message.MessageType;

public class DeleteHandler implements MessageHandler {
        private Message clientMessage;

        public DeleteHandler(Message clientMessage) {
            this.clientMessage = clientMessage;
        }


        @Override
        public void run() {
            try {
                if (clientMessage.getMessageType() == MessageType.DELETE) {
                    DeleteMessage dm = (DeleteMessage) clientMessage;

                    int key = dm.getKey();
                    String value = dm.getPath();
                    int originalPort = dm.getOriginalSenderPort();

                    AppConfig.chordState.deleteValue(key, value, originalPort);
                } else {
                    AppConfig.timestampedErrorPrint("Delete handler got a message that is not DELETE");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
