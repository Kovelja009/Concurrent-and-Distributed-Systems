package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.UPDATE) {
			if (clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) { // if I need to add new node
				try {
					// here each node in the system is informed about the new node
					// and is adding it to its list of nodes
					ServentInfo newNodInfo = new ServentInfo("localhost", clientMessage.getSenderPort());
					List<ServentInfo> newNodes = new ArrayList<>();
					newNodes.add(newNodInfo);
					AppConfig.chordState.addNodes(newNodes);

					////////////////////////////////////////////////////////////////////
	//				Message format: "port1, port2, port3 - |n1, n2, n3, n4|"
					////////////////////////////////////////////////////////////////////
					// beside appending my port, also update message rn and append it to message (at the end new node will
					// have all the info about all nodes in the system)
					// rn_sending = max(rn, rn_received)

					String messageText = clientMessage.getMessageText();
					String newMessageText = updateMessage(messageText);

					Message nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
							newMessageText);
					MessageUtil.sendMessage(nextUpdate);
				} catch (Exception e) {
					e.printStackTrace();
				}
				////////////////////////////////////////////////////////////////////
			} else { // if all nodes are already informed about me, so the message is coming back to me
				try {
					List<Integer> portsFromMessage = getPortsFromMessage(clientMessage.getMessageText());
					List<Integer> rnsFromMessage = getRnsFromMessage(clientMessage.getMessageText());

//					AppConfig.timestampedStandardPrint("Got update image trough whole circle: " + portsFromMessage + " - " + rnsFromMessage);

					//////////////////////////////////////////////
					// add info about all nodes in the system
					List<ServentInfo> allNodes = new ArrayList<>();
					for (Integer port : portsFromMessage)
						allNodes.add(new ServentInfo("localhost", port));

					AppConfig.chordState.addNodes(allNodes);
					//////////////////////////////////////////////

					// update my RN in Suzuki-Kasami
					updateRNs(rnsFromMessage);

					// release Suzuki-Kasami token
					AppConfig.chordState.getSuzukiKasamiUtils().distributedUnlock();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
		}
	}

//	Message format: "port1, port2, port3 - |n1, n2, n3, n4|"
	private String updateMessage(String message) {
		String[] parts = message.split("-");

		// ports can be empty, but rns can't
		String[] ports = parts[0].split(",");
		// for rn also delete any leading or trailing whitespaces
		String[] rns = parts[1].replace("|", "").trim().split(",");

		/////////////////////////////////////////////////////////////
		// PORTS LOGIC
		// number of ports (if only empty str, it is 0)
		int n_ports = ports.length;
		if(n_ports == 1 && ports[0].isBlank() || (ports[0].strip().equals("_"))) {
			n_ports = 0;
		}

		// update port list with me
		String portsStr = "";
		for (int i = 0; i < n_ports; i++)
			portsStr += ports[i] + ",";

		portsStr += AppConfig.myServentInfo.getListenerPort();

		/////////////////////////////////////////////////////////////
		// RN LOGIC

		// number of rns
		int n_rns = rns.length;

		// update with following formula: rn_sending = max(rn, rn_received)

		List<Integer> rn_sending = new ArrayList<>();

		for(int i = 0; i < n_rns; i++) {
			int rn = AppConfig.chordState.getSuzukiKasamiUtils().getRn().get(i);
			int rn_received = Integer.parseInt(rns[i]);
			rn_sending.add(Math.max(rn, rn_received));
		}

		// make string from rn_sending
		String rn_sending_str = stringifyRn(rn_sending);

		return portsStr + "-" + rn_sending_str;
	}

	private String stringifyRn(List<Integer> rn) {
		String result = "";
		for (Integer i : rn) {
			result += i + ",";
		}

		// remove trailing comma if it exists
		if (result.charAt(result.length() - 1) == ',') {
			result = result.substring(0, result.length() - 1);
		}

		// add the leading and trailing '|'
		result = "|" + result + "|";

		return result;
	}

	private List<Integer> getPortsFromMessage(String message) {
		String[] parts = message.split("-");
		String[] ports = parts[0].split(",");

		List<Integer> result = new ArrayList<>();
		for (String port : ports) {
			result.add(Integer.parseInt(port));
		}

		return result;
	}

	private List<Integer> getRnsFromMessage(String message) {
		String[] parts = message.split("-");
		String[] rns = parts[1].replace("|", "").trim().split(",");

		List<Integer> result = new ArrayList<>();
		for (String rn : rns) {
			result.add(Integer.parseInt(rn));
		}

		return result;
	}

	private void updateRNs(List<Integer> rns) {
		for (int i = 0; i < rns.size(); i++) {
			int rn = AppConfig.chordState.getSuzukiKasamiUtils().getRn().get(i);
			int rn_received = rns.get(i);
			AppConfig.chordState.getSuzukiKasamiUtils().getRn().set(i, Math.max(rn, rn_received));
		}

		// print updated values
//		AppConfig.timestampedStandardPrint("When I entered the system: " + AppConfig.chordState.getSuzukiKasamiUtils().getRn());
	}

}
