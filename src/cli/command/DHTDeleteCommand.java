package cli.command;

import app.AppConfig;
import app.ServentInfo;

public class DHTDeleteCommand implements CLICommand{

        @Override
        public String commandName() {
            return "delete_file";
        }

    @Override
    public void execute(String args) {
        try {
            String path = args;
            int key = AppConfig.chordState.hashFileName(path);
            // distributed lock
            AppConfig.chordState.getSuzukiKasamiUtils().distributedLock(AppConfig.chordState.getAllNodeInfo().stream().map(ServentInfo::getListenerPort).toList(), false);

            AppConfig.chordState.deleteValue(key, path, AppConfig.myServentInfo.getListenerPort());
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Invalid argument for dht_delete: " + args + ". Should be key, which is an int.");
        }
    }
}
