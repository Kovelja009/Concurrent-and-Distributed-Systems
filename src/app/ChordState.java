package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import app.suzuki_kasami.SuzukiKasamiUtils;
import servent.message.*;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;

	private SuzukiKasamiUtils suzukiKasamiUtils;

	// list of friends (represents which nodes that I can read private files from)
	// must be concurrent safe
	private List<Integer> friends;

	// pay attention that inner hashmap can be NULL!
	private Map<Integer, Map<String, MetaFile>> valueMap;

	// because messages are not FIFO (used for backup)
	private List<String> filesToBeDeleted;

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();

		suzukiKasamiUtils = new SuzukiKasamiUtils(CHORD_SIZE, AppConfig.myServentInfo.getChordId());
		friends = new CopyOnWriteArrayList<>();
		filesToBeDeleted = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		this.valueMap = welcomeMsg.getValues();
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Map<String, MetaFile>> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Map<String, MetaFile>> valueMap) {
		this.valueMap = valueMap;
	}

	public SuzukiKasamiUtils getSuzukiKasamiUtils() {
		return suzukiKasamiUtils;
	}

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	/**
	 * The Chord delete operation. Deletes locally if key is ours, otherwise sends it on.
	 */
	// TODO: delete backup at neighbour also
	public void deleteValue(int key, String value, int port){
		if(isKeyMine(key)){
			//////////////////////////////////
			int deleted = deleteIfPossible(key, value, port);

			// if we succesfully deleted file, also neighbour should delete the backup
			if (deleted == 0) {
				deletingNeighbourBackup(key, value);
			}
			//////////////////////////////////

			// if we are the one deleting the value, then we will unlock
			if(port == AppConfig.myServentInfo.getListenerPort()) {
				suzukiKasamiUtils.distributedUnlock();
			} else { // else we will send delete_unlock message to the node who requested delete
				DeleteUnlockMessage dum = new DeleteUnlockMessage(AppConfig.myServentInfo.getListenerPort(), port, value, deleted);
				MessageUtil.sendMessage(dum);
			}
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			DeleteMessage dm = new DeleteMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, value, port);
			MessageUtil.sendMessage(dm);

		}
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 * When storing locally, we also send the value to our neighbours.
	 * */
	public void putValue(int key, String value, int port, boolean isPublic) {
		if (isKeyMine(key)) {
			// putting value into hashmap
			MetaFile metaFile = putIntoHashMap(key, value, port, isPublic);
			AppConfig.timestampedStandardPrint("ADDED FILE: {" + metaFile.getPath() + "} owned by: " + metaFile.getOwnerPort());

			// send value to neighbours (predecessor and successor)
			sendingToNeighbours(metaFile);

			// if we are the one putting the value, then we will unlock
			if(port == AppConfig.myServentInfo.getListenerPort()) {
				suzukiKasamiUtils.distributedUnlock();
			} else { // else we will send put_unlock message to the node who requested put
				PutUnlockMessage pum = new PutUnlockMessage(AppConfig.myServentInfo.getListenerPort(), port);
				MessageUtil.sendMessage(pum);
			}

		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, port, value, isPublic);
			MessageUtil.sendMessage(pm);
		}
	}

	public int deleteIfPossible(int key, String value, int port) {
		Map<String, MetaFile> map = valueMap.get(key);
		MetaFile deleted = null;
		int res;

		if(map == null) {
			AppConfig.timestampedStandardPrint("DELETE: map is null");
			res = -2;
		} else {
			// we can delete only if we are the owner of the file
			boolean canDelete = map.get(value).getOwnerPort() == port;
			if (canDelete){
				deleted = map.remove(value);
				if (deleted != null) {
					AppConfig.timestampedStandardPrint("DELETE: " + deleted.getPath() + " owned by: " + deleted.getOwnerPort());
					res = 0;
				} else {
					AppConfig.timestampedStandardPrint("DELETE: " + value + " not found");
					res = -3;
				}
			} else {
				// we are not the owner of the file
				AppConfig.timestampedStandardPrint("DELETE: " + value + " is not owned by " + port + " hence cannot be deleted");
				res = -1;
			}
		}

		return res;
	}

	private void deletingNeighbourBackup(int key, String value) {
		// send to predecessor
		if(predecessorInfo != null) {
			DeleteBackupMessage dbm = new DeleteBackupMessage(AppConfig.myServentInfo.getListenerPort(), predecessorInfo.getListenerPort(), key, value);
			MessageUtil.sendMessage(dbm);
		}

		// send to successor
		if (successorTable[0] != null) {
			DeleteBackupMessage dbm = new DeleteBackupMessage(AppConfig.myServentInfo.getListenerPort(), successorTable[0].getListenerPort(), key, value);
			MessageUtil.sendMessage(dbm);
		}
	}

	public void deleteBackup(String path) {
		if(!filesToBeDeleted.contains(path))
			filesToBeDeleted.add(path);
		deletion();
	}

	public void deletion() {
		// go through all files that need to be deleted, and if they are successfully deleted, remove them from the list
		List<String> tmp = new CopyOnWriteArrayList<>();
		for (String path : filesToBeDeleted) {
			int key = hashFileName(path);
			Map<String, MetaFile> map = valueMap.get(key);
			boolean shouldRemove = true;
			if (map != null) {
				MetaFile deleted = map.remove(path);
				if (deleted != null) {
					AppConfig.timestampedStandardPrint("DELETED BACKUP: " + deleted.getPath() + " owned by: " + deleted.getOwnerPort());
					shouldRemove = false;
				}
			}

			if (!shouldRemove)
				tmp.add(path);
		}
		filesToBeDeleted = tmp;
	}

	public MetaFile putIntoHashMap(int key, String path, int port, boolean isPublic) {
		// get map for the key or create new map if it doesn't exist
        Map<String, MetaFile> map = valueMap.computeIfAbsent(key, k -> new HashMap<>());

        // create new MetaFile object and put it in the map
		MetaFile metaFile = new MetaFile(path, port, isPublic);
		map.putIfAbsent(path, metaFile);
		return metaFile;
	}

	private void sendingToNeighbours(MetaFile metaFile) {
		// send to predecessor
		if(predecessorInfo != null) {
			BackupMessage bm = new BackupMessage(AppConfig.myServentInfo.getListenerPort(), predecessorInfo.getListenerPort(), metaFile);
			MessageUtil.sendMessage(bm);
		}

		// send to successor
		if (successorTable[0] != null) {
			BackupMessage bm = new BackupMessage(AppConfig.myServentInfo.getListenerPort(), successorTable[0].getListenerPort(), metaFile);
			MessageUtil.sendMessage(bm);
		}
	}


	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */

	public MetaFile getValue(int key, String path) {
		// distributed lock
		suzukiKasamiUtils.distributedLock(AppConfig.chordState.getAllNodeInfo().stream().map(ServentInfo::getListenerPort).toList());

		if (isKeyMine(key)) {
			// distributed unlock (it was mine to begin with, so I can unlock it now)
			suzukiKasamiUtils.distributedUnlock();

			MetaFile notFound = new MetaFile(path, -1, false);
			if (valueMap.containsKey(key)) // we have hashmap for the key
                return valueMap.get(key).getOrDefault(path, notFound);
			else // hashmap in null
				return notFound;
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, path);
		MessageUtil.sendMessage(agm);
		
		return new MetaFile(path, -2, false);
	}

	public void addFriend(int port) {
		friends.add(port);
	}


	public List<Integer> getFriends() {
		return friends;
	}

	public boolean canRead(MetaFile file) {
		return file.isPublic() || amSubscribed(file.getOwnerPort());
	}

	private boolean amSubscribed(int port){
		return friends.contains(port);
	}

	public List<ServentInfo> getAllNodeInfo() {
		return allNodeInfo;
	}

	public Integer hashFileName(String path) {
		return path.hashCode() % CHORD_SIZE;
	}

}
