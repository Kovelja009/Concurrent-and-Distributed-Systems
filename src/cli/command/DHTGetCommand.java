package cli.command;

import app.AppConfig;
import app.MetaFile;

public class DHTGetCommand implements CLICommand {

	@Override
	public String commandName() {
		return "view_file";
	}

	@Override
	public void execute(String args) {
		try {
			String path = args;
			int key = AppConfig.chordState.hashFileName(path);
			MetaFile file = AppConfig.chordState.getValue(key, path);
			
			if (file.getOwnerPort() == -2) {
				AppConfig.timestampedStandardPrint("Please wait while someone grabs the file...");
			} else if (file.getOwnerPort()== -1) {
				AppConfig.timestampedStandardPrint("No such file in the system with path: " + path);
			} else {
				if(AppConfig.chordState.canRead(file))
					AppConfig.timestampedStandardPrint(path + ": CONTENT -> " + AppConfig.readTextFile(path));
				else
					AppConfig.timestampedStandardPrint("Don't have permission to read file: " + path);
			}
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Invalid argument for dht_get: " + args + ". Should be key, which is an int.");
		}
	}

}
