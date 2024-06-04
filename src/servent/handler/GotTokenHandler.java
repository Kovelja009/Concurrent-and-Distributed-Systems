package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SendSuzukiTokenMessage;

public class GotTokenHandler implements MessageHandler{

    private Message clientMessage;

    public GotTokenHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }


    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SEND_SUZUKI_TOKEN) {
            try {
                SendSuzukiTokenMessage sendSuzukiTokenMessage = (SendSuzukiTokenMessage) clientMessage;
                AppConfig.chordState.getSuzukiKasamiUtils().setToken(sendSuzukiTokenMessage.getToken());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            AppConfig.timestampedErrorPrint("Got to token handler but message is not SEND_SUZUKI_TOKEN");
        }
    }
}
