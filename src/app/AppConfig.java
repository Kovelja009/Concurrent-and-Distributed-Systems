package app;

import cli.CLIParser;
import servent.SimpleServentListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;

	public static String rootPath;

	private static List<String> goodbyeMessages;

	private static CLIParser parser;
	private static SimpleServentListener listener;

	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " - " + message);
	}

	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.err.println(timeFormat.format(now) + " - " + message);
	}

	public static boolean INITIALIZED = false;
	public static int BOOTSTRAP_PORT;
	public static int SERVENT_COUNT;

	public static ChordState chordState;

	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 *
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. A bootstrap server listening on port 2000, and Chord system with
	 * max 64 keys and 64 nodes.<br/>
	 *
	 * @param configName name of configuration file
	 * @param serventId id of the servent, as used in the configuration file
	 */
	public static void readConfig(String configName, int serventId){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		try {
			SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(0);
		}

		try {
			int chordSize = Integer.parseInt(properties.getProperty("chord_size"));

			ChordState.CHORD_SIZE = chordSize;

		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
			System.exit(0);
		}

		String portProperty = "servent"+serventId+".port";

		int serventPort = -1;

		try {
			serventPort = Integer.parseInt(properties.getProperty(portProperty));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
			System.exit(0);
		}

		try {
			rootPath = properties.getProperty("root_path");
		} catch (Exception e) {
			timestampedErrorPrint("Problem reading root_path. Exiting...");
			System.exit(0);
		}

		myServentInfo = new ServentInfo("localhost", serventPort);
		chordState = new ChordState();
		initGoodbyeMessages();
	}


	public static boolean isFileValid(String pathFromRoot) {
		try {
			// concat root path with the path from root
			File file = new File(rootPath + "/" + pathFromRoot);
			return file.exists() && !file.isDirectory();
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("Error while checking file validity for path: " + pathFromRoot);
			return false;
		}
	}

	public static String readTextFile(String pathFromRoot) {
		try {
			File file = new File(rootPath + "/" + pathFromRoot);
			byte[] encoded = java.nio.file.Files.readAllBytes(file.toPath());
			return new String(encoded);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Error while reading file: " + pathFromRoot);
			return null;
		}
	}

	public static String getRandomMessage(){
		return goodbyeMessages.get((int) (Math.random() * goodbyeMessages.size()));
	}

	private static void initGoodbyeMessages() {
		goodbyeMessages = new ArrayList<>();
		goodbyeMessages.add("You have no enemies.");
		goodbyeMessages.add("Sometimes good people make bad choices. It doesn't mean they are bad people. It means they're human.");
		goodbyeMessages.add("In choosing both, you lose both.");
		goodbyeMessages.add("Everyone had to be drunk on somethin’ to keep pushing on");
		goodbyeMessages.add("Give up on your Dreams and Die");
		goodbyeMessages.add("What a beautiful day it is, it's a shame I didn't realize it sooner");
		goodbyeMessages.add("My soldiers rage! My soldiers scream! My soldiers fight!");
		goodbyeMessages.add("This World Is Cruel, And It’s Also Very Beautiful.");
		goodbyeMessages.add("I want to see and understand the world outside. I don’t want to die inside these walls without knowing what’s out there!");
		goodbyeMessages.add("I’m not going to die. Not until I’ve seen it. The sea!");
	}

	public static void setParserAndListener(CLIParser parser, SimpleServentListener listener) {
		AppConfig.parser = parser;
		AppConfig.listener = listener;
	}

	public static CLIParser getParser() {
		return parser;
	}

	public static SimpleServentListener getListener() {
		return listener;
	}

}
