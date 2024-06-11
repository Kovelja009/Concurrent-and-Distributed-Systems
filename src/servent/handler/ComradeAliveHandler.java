package servent.handler;

import servent.message.ComradeAliveMessage;
import servent.message.ComradeAnswerMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ComradeAliveHandler implements MessageHandler {
    private Message clientMessage;

    public ComradeAliveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.COMRADE_ALIVE) {
                 ComradeAliveMessage cam = (ComradeAliveMessage) clientMessage;

                ComradeAnswerMessage caa = new ComradeAnswerMessage(cam.getSenderPort(), cam.getInitPort(), cam.getTryPort(), cam.getInitPort());
                MessageUtil.sendMessage(caa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
