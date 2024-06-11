package servent.message;

public class ComradeContactMessage extends BasicMessage {

    private static final long serialVersionUID = 17863313L;
    private int tryPort;
    private int initPort;

    public ComradeContactMessage(int senderPort, int receiverPort, int tryPort, int initPort) {
        super(MessageType.COMRADE_CONTACT, senderPort, receiverPort);
        this.tryPort = tryPort;
        this.initPort = initPort;
    }

    public int getTryPort() {
        return tryPort;
    }

    public int getInitPort() {
        return initPort;
    }
}
