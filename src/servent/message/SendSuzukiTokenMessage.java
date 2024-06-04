package servent.message;

import app.suzuki_kasami.SuzukiKasamiToken;

public class SendSuzukiTokenMessage extends BasicMessage{

    private static final long serialVersionUID = 3444837287772127636L;
    private SuzukiKasamiToken token;

    public SendSuzukiTokenMessage(int senderPort, int receiverPort, SuzukiKasamiToken token) {
        super(MessageType.SEND_SUZUKI_TOKEN, senderPort, receiverPort);
        this.token = token;
    }

    public SuzukiKasamiToken getToken() {
        return token;
    }
}
