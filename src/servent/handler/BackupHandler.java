package servent.handler;

import app.AppConfig;
import app.MetaFile;
import servent.message.BackupMessage;
import servent.message.Message;
import servent.message.MessageType;

public class BackupHandler implements MessageHandler{
    private Message clientMessage;

    public BackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.BACKUP) {
                BackupMessage backupMessage = (BackupMessage) clientMessage;
                MetaFile metaFile = backupMessage.getMetaFile();

                int key = AppConfig.chordState.hashFileName(metaFile.getPath());
                String path = metaFile.getPath();
                int originalPort = metaFile.getOwnerPort();
                boolean isPublic = metaFile.isPublic();

                AppConfig.chordState.putIntoHashMap(key, path, originalPort, isPublic);
                AppConfig.timestampedStandardPrint("Backup of file {" + path + "} completed.");

            } else {
                AppConfig.timestampedErrorPrint("Backup handler got a message that is not BACKUP");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
