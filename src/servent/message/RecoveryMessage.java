package servent.message;

import app.ServentInfo;

import java.util.List;

public class RecoveryMessage extends BasicMessage {

    private static final long serialVersionUID = 4342785L;
    private ServentInfo toBeDeleted;
    private int tokenHolder;

    public RecoveryMessage(int senderPort, int receiverPort, ServentInfo toBeDeleted) {
        super(MessageType.RECOVERY, senderPort, receiverPort, "recovery");
        this.toBeDeleted = toBeDeleted;
    }

    public ServentInfo getToBeDeleted() {
        return toBeDeleted;
    }

    public void toBeDeleted(ServentInfo toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }

    public int getTokenHolderPort() {
        return tokenHolder;
    }

    public void setTokenHolderPort(int tokenHolder) {
        this.tokenHolder = tokenHolder;
    }


}
