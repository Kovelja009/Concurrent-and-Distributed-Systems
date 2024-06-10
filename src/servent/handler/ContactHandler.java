package servent.handler;

import servent.message.ContactSuccessMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ContactHandler implements MessageHandler{

    private Message clientMessage;

    public ContactHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if(clientMessage.getMessageType() == MessageType.CONTACT) {
                // send contact success message
                ContactSuccessMessage csm = new ContactSuccessMessage(clientMessage.getReceiverPort(), clientMessage.getSenderPort());
                MessageUtil.sendMessage(csm);

            } else {
                System.out.println("Contact handler got a message that is not CONTACT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
