package servent.handler;

import app.AppConfig;
import app.ChordState;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RequestSuzukiTokenMessage;

public class SuzukiRequestHandler implements MessageHandler{

    private Message clientMessage;

    public SuzukiRequestHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }


    @Override
    public void run() {
        try {

            if (clientMessage.getMessageType() == MessageType.REQUEST_SUZUKI_TOKEN) {
                RequestSuzukiTokenMessage requestSuzukiTokenMessage = (RequestSuzukiTokenMessage) clientMessage;
                Integer senderRN = requestSuzukiTokenMessage.getSenderRN();
                Integer senderChordID = ChordState.chordHash(requestSuzukiTokenMessage.getSenderPort());

                if (AppConfig.chordState.getSuzukiKasamiUtils().getRn().get(senderChordID) < senderRN) {
                    // update senders RN
                    AppConfig.chordState.getSuzukiKasamiUtils().getRn().set(senderChordID, senderRN);

                    // if we have token and not in critical section then send token
                    if (AppConfig.chordState.getSuzukiKasamiUtils().getHasSuzukiToken().get() &&
                            AppConfig.chordState.getSuzukiKasamiUtils().getToken().getLn().get(senderChordID) + 1 == senderRN) {

                        // add it to queue
                        AppConfig.chordState.getSuzukiKasamiUtils().getToken().getQueue().add(requestSuzukiTokenMessage.getSenderPort());

                        // if we are not in critical section then send token
                        if (!AppConfig.chordState.getSuzukiKasamiUtils().getUsesSuzukiToken().get())
                            AppConfig.chordState.getSuzukiKasamiUtils().possibleSendingToken();
                    }

                }
            } else {
                AppConfig.timestampedErrorPrint("Got to token request handler but message is not REQUEST_SUZUKI_TOKEN");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
