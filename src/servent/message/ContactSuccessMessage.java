package servent.message;

public class ContactSuccessMessage extends BasicMessage {

    private static final long serialVersionUID = 4342785233221L;

    public ContactSuccessMessage(int senderPort, int receiverPort) {
        super(MessageType.CONTACT_SUCCESS, senderPort, receiverPort, "contact success");
    }
}
