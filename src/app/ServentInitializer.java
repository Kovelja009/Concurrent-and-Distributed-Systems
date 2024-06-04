package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import app.suzuki_kasami.SuzukiKasamiToken;
import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private static int someServentPort;
	private static List<Integer> broadcastPorts = new ArrayList<>();

	private void parseMessagePortsAndNeighbours() {
		int bsPort = AppConfig.BOOTSTRAP_PORT;
		
		int randomPort = -2;
		
		try {
			Socket bsSocket = new Socket("localhost", bsPort);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();

			// Message format: randomPort-|port1, port2, port3|\n
			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			String message = bsScanner.nextLine();

			// Step 1: Split the message into parts
			String[] parts = message.split("_");

			// Step 2: Extract randomPort
			randomPort = Integer.parseInt(parts[0]);

			// Step 3: Process the second part to remove the trailing '|' and split by commas
			String portListString = parts[1].replace("|", "").trim();

			if (!portListString.isEmpty()) {
				String[] portStrings = portListString.split(",\\s*"); // split by comma and optional whitespace

				for (String portString : portStrings){
					int port = Integer.parseInt(portString);
					if (!broadcastPorts.contains(port))
						broadcastPorts.add(port);
				}
			}


			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		someServentPort = randomPort;
	}
	
	@Override
	public void run() {
		parseMessagePortsAndNeighbours();
		while(someServentPort == -3) { //bootstrap told us to wait
			try {
				Thread.sleep(500);
				parseMessagePortsAndNeighbours();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (someServentPort == -2) { //bootstrap is not working
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}
		if (someServentPort == -1) { //bootstrap gave us -1 -> we are first
			AppConfig.timestampedStandardPrint("First node in Chord system.");
			// we create Suzuki-Kasami token here
			// also set that we have token but not using it
			SuzukiKasamiToken token = new SuzukiKasamiToken(AppConfig.chordState.CHORD_SIZE);
			AppConfig.chordState.getSuzukiKasamiUtils().setToken(token);
			AppConfig.chordState.getSuzukiKasamiUtils().getHasSuzukiToken().set(true);

			notifyBootStrap();

		} else { //bootstrap gave us something else - let that node tell our successor that we are here
			// first we need info about all nodes in the system from the bootstrap
			// Suzuki lock
			AppConfig.timestampedStandardPrint("Requiring Suzuki-Kasami token");
			AppConfig.chordState.getSuzukiKasamiUtils().distributedLock(broadcastPorts);
			AppConfig.timestampedStandardPrint("Got token");

			NewNodeMessage nnm = new NewNodeMessage(AppConfig.myServentInfo.getListenerPort(), someServentPort);
			MessageUtil.sendMessage(nnm);
		}
	}

	private void notifyBootStrap() {
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("FirstNode\n");
			bsWriter.flush();

			bsSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
			AppConfig.timestampedErrorPrint("First node notify bootstrap failed.");
		}
	}

}
