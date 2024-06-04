package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BootstrapServer {

	private volatile boolean working = true;
	private List<Integer> activeServents;
	private AtomicBoolean sync = new AtomicBoolean(false);
	
	public BootstrapServer() {
		activeServents = new ArrayList<>();
	}
	
	public void doBootstrap(int bsPort) {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(bsPort);
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			AppConfig.timestampedErrorPrint("Problem while opening listener socket.");
			System.exit(0);
		}
		
		Random rand = new Random(System.currentTimeMillis());
		
		while (working) {
			try {
				Socket newServentSocket = listenerSocket.accept();

				 /*
				 * Handling these messages is intentionally sequential, to avoid problems with
				 * concurrent initial starts.
				 *
				 * In practice, we would have an always-active backbone of servents to avoid this problem.
				 */
				
				Scanner socketScanner = new Scanner(newServentSocket.getInputStream());
				String message = socketScanner.nextLine();
				
				/*
				 * New servent has hailed us. He is sending us his own listener port.
				 * He wants to get a listener port from a random active servent,
				 * or -1 if he is the first one or -3 if he has to wait before initialization.
				 *
				 * Message format: randomPort_|port1, port2, port3|\n
				 *
				 */
				if (message.equals("Hail")) {
					int newServentPort = socketScanner.nextInt();

//					AppConfig.timestampedStandardPrint("Tried to enter: " + newServentPort);
					PrintWriter socketWriter = new PrintWriter(newServentSocket.getOutputStream());

					// hold synchronization
					boolean shouldAdd = sync.compareAndSet(false, true);
					// someone is joining the network before us, so we must wait
					if (!shouldAdd) {
//						AppConfig.timestampedStandardPrint("Waiting to join: " + newServentPort);
						socketWriter.write(buildMessage(-3));
					} else {
						if (activeServents.size() == 0) { // he is the first one
							socketWriter.write(buildMessage(-1));
							activeServents.add(newServentPort);
							AppConfig.timestampedStandardPrint("Adding first node: " + newServentPort);
						} else { // we are giving him a random servent (we are not first in the system)
							// but that node also requires ports for broadcast in order to get distributed lock
							// so we need to append all active servents

							int randServent = activeServents.get(rand.nextInt(activeServents.size()));
							String msg = buildMessage(randServent);
							AppConfig.timestampedStandardPrint("Sending message to " + newServentPort+ ": " + msg);
							socketWriter.write(msg);
						}
					}

					socketWriter.flush();
					newServentSocket.close();
				} else if (message.equals("New")) {
					/**
					 * When a servent is confirmed not to be a collider, we add him to the list.
					 */
					int newServentPort = socketScanner.nextInt();
					
					AppConfig.timestampedStandardPrint("Adding: " + newServentPort);
					
					activeServents.add(newServentPort);
					newServentSocket.close();
					// release synchronization
					sync.set(false);
					AppConfig.timestampedStandardPrint("Released sync: " + newServentPort);
				} else if (message.equals("Sorry")) {
					int newServentPort = socketScanner.nextInt();

					// release synchronization
					sync.set(false);
					AppConfig.timestampedStandardPrint("Released sync: " + newServentPort);
				} else if(message.equals("FirstNode")) {
					// release synchronization
					sync.set(false);
					AppConfig.timestampedStandardPrint("Released sync: first node");
				}
				
			} catch (SocketTimeoutException e) {
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String buildMessage(int randServent){
		StringBuilder sb = new StringBuilder();
		sb.append(randServent).append("_|");
		for (Integer serventPort : activeServents) {
			sb.append(serventPort).append(",");
		}
		if(sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);
		sb.append("|\n");

		return sb.toString();
	}
	
	/**
	 * Expects one command line argument - the port to listen on.
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			AppConfig.timestampedErrorPrint("Bootstrap started without port argument.");
		}
		
		int bsPort = 0;
		try {
			bsPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Bootstrap port not valid: " + args[0]);
			System.exit(0);
		}
		
		AppConfig.timestampedStandardPrint("Bootstrap server started on port: " + bsPort);
		
		BootstrapServer bs = new BootstrapServer();
		bs.doBootstrap(bsPort);
	}
}
