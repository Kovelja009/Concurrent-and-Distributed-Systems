package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;

import java.util.List;

public class DHTPutCommand implements CLICommand {

	@Override
	public String commandName() {
		return "add_file";
	}

	@Override
	public void execute(String args) {
		String[] splitArgs = args.split(" ");
		if (splitArgs.length != 2) {
			AppConfig.timestampedErrorPrint("PUT COMMAND: Invalid number of arguments. Should be 2.");
			return;
		}

		String path = splitArgs[0];
		boolean isPublic = splitArgs[1].equalsIgnoreCase("public");

		// check if second arg is public/private (equals ignore case) and if it isn't print error, but if it is
		if (!isPublic && !splitArgs[1].equalsIgnoreCase("private")) {
			AppConfig.timestampedErrorPrint("PUT COMMAND: Invalid second argument. Should be 'public' or 'private'.");
			return;
		}
		
		if (AppConfig.isFileValid(path)) {
			int key = AppConfig.chordState.hashFileName(path);

			// request Suzuki-Kasami distributed lock
			AppConfig.chordState.getSuzukiKasamiUtils().distributedLock(AppConfig.chordState.getAllNodeInfo().stream().map(ServentInfo::getListenerPort).toList());

			AppConfig.chordState.putValue(key, path, AppConfig.myServentInfo.getListenerPort(), isPublic);
		} else {
			AppConfig.timestampedErrorPrint("PUT COMMAND: Invalid file path: " + path);
		}

	}

}
