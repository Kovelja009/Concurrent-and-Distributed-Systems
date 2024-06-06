package cli.command;

import app.AppConfig;

public class DHTFriendCommand implements CLICommand {
    @Override
    public String commandName() {
        return "subscribe";
    }

    @Override
    public void execute(String args) {
        try {
            int port = Integer.parseInt(args);
            AppConfig.chordState.addFriend(port);

            AppConfig.timestampedStandardPrint("Subscribed to port: " + port + ", my current friends: " + AppConfig.chordState.getFriends());

        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Invalid argument for subscribe: " + args + ". Should be port, which is an int.");
        }
    }
}
