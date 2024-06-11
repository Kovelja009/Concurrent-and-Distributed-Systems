package servent.handler;

import app.AppConfig;
import servent.message.ComradeAliveMessage;
import servent.message.ComradeContactMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ComradeContactHandler implements MessageHandler {
    private Message clientMessage;

    public ComradeContactHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.COMRADE_CONTACT) {
                ComradeContactMessage ccm = (ComradeContactMessage) clientMessage;

                ComradeAliveMessage cam = new ComradeAliveMessage(AppConfig.myServentInfo.getListenerPort(),
                        ccm.getSenderPort(), ccm.getTryPort(), ccm.getInitPort());

                MessageUtil.sendMessage(cam);
            } else {
                AppConfig.timestampedErrorPrint("ComradeContact handler got a message that is not COMRADE_CONTACT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
