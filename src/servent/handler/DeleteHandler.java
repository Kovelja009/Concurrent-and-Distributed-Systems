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
                    String[] splitText = clientMessage.getMessageText().split(":");
                    if (splitText.length == 2) {
                        int key = 0;
                        int value = 0;
                        try {
                            key = Integer.parseInt(splitText[0]);
                            value = Integer.parseInt(splitText[1]);
                            int originalPort = ((DeleteMessage)clientMessage).getOriginalSenderPort();

                            AppConfig.chordState.deleteValue(key, value, originalPort);
                        } catch (NumberFormatException e) {
                            AppConfig.timestampedErrorPrint("Got delete message with bad text: " + clientMessage.getMessageText());
                        }
                    } else {
                        AppConfig.timestampedErrorPrint("Got delete message with bad text: " + clientMessage.getMessageText());
                    }
                } else {
                    AppConfig.timestampedErrorPrint("Delete handler got a message that is not DELETE");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
