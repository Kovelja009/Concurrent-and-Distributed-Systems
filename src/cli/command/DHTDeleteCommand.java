package cli.command;

import app.AppConfig;
import app.ServentInfo;

public class DHTDeleteCommand implements CLICommand{

        @Override
        public String commandName() {
            return "dht_delete";
        }

    @Override
    public void execute(String args) {
        try {
            int key = Integer.parseInt(args);
            // TODO: change this :(

            // distributed lock
            AppConfig.chordState.getSuzukiKasamiUtils().distributedLock(AppConfig.chordState.getAllNodeInfo().stream().map(ServentInfo::getListenerPort).toList());
            AppConfig.chordState.deleteValue(key, 0, AppConfig.myServentInfo.getListenerPort());
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Invalid argument for dht_delete: " + args + ". Should be key, which is an int.");
        }
    }
}
