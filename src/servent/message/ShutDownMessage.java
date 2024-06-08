package servent.message;

import app.ServentInfo;
import app.suzuki_kasami.SuzukiKasamiToken;

public class ShutDownMessage extends BasicMessage{
    private static final long serialVersionUID = 789L;
    private ServentInfo shuttingDownServentInfo;
    private ServentInfo predecessorServentInfo;
    private ServentInfo successorServentInfo;
    private SuzukiKasamiToken token;

    public ShutDownMessage(int senderPort, int receiverPort, ServentInfo shuttingDownServentInfo, ServentInfo predecessorServentInfo, ServentInfo successorServentInfo, SuzukiKasamiToken token) {
        super(MessageType.SHUT_DOWN, senderPort, receiverPort, "");
        this.shuttingDownServentInfo = shuttingDownServentInfo;
        this.predecessorServentInfo = predecessorServentInfo;
        this.successorServentInfo = successorServentInfo;
        this.token = token;
    }

    public ServentInfo getShuttingDownServentInfo() {
        return shuttingDownServentInfo;
    }

    public ServentInfo getPredecessorServentInfo() {
        return predecessorServentInfo;
    }

    public ServentInfo getSuccessorServentInfo() {
        return successorServentInfo;
    }
    public SuzukiKasamiToken getToken() {
        return token;
    }
}
