package servent.message;

public class ContactMessage extends BasicMessage{

    private static final long serialVersionUID = 4342785234234L;

    public ContactMessage(int senderPort, int receiverPort) {
        super(MessageType.CONTACT, senderPort, receiverPort, "contact");
    }
}
