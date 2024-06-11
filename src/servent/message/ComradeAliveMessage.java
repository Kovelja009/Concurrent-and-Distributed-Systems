package servent.message;

public class ComradeAliveMessage extends BasicMessage {

        private static final long serialVersionUID = 17863313432L;
        private int tryPort;
        private int initPort;

        public ComradeAliveMessage(int senderPort, int receiverPort, int tryPort, int initPort) {
            super(MessageType.COMRADE_ALIVE, senderPort, receiverPort);
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
