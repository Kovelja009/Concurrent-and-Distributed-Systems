package servent.handler;

import app.AppConfig;
import servent.message.ComradeAskMessage;
import servent.message.ComradeContactMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ComradeAskHandler implements MessageHandler {
    private Message clientMessage;

    public ComradeAskHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.COMRADE_ASK) {
                ComradeAskMessage cam = (ComradeAskMessage) clientMessage;

                ComradeContactMessage ccm = new ComradeContactMessage(AppConfig.myServentInfo.getListenerPort(),
                        cam.getTryPort(), cam.getTryPort(), cam.getInitPort());

                MessageUtil.sendMessage(ccm);
            } else {
                AppConfig.timestampedErrorPrint("ComradeAsk handler got a message that is not COMRADE_ASK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
