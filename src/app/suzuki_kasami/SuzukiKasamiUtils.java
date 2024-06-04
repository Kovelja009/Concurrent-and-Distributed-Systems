package app.suzuki_kasami;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.RequestSuzukiTokenMessage;
import servent.message.SendSuzukiTokenMessage;
import servent.message.util.MessageUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SuzukiKasamiUtils {
    // need atomic boolean for critical section
    private AtomicBoolean localLock;
    private int nodeId;


    private AtomicBoolean hasSuzukiToken;
    private AtomicBoolean usesSuzukiToken;
    private SuzukiKasamiToken token;


    // request number
    private CopyOnWriteArrayList<Integer> rn;

    public SuzukiKasamiUtils(int numNodes, int nodeId) {
        this.nodeId = nodeId;
        
        // initialize request number with 0
        this.rn = new CopyOnWriteArrayList<Integer>();
        for (int i = 0; i < numNodes; i++)
            this.rn.add(0);

        this.localLock = new AtomicBoolean(false);
        this.hasSuzukiToken = new AtomicBoolean(false);
        this.usesSuzukiToken = new AtomicBoolean(false);
    }

    public void distributedLock(List<Integer> broadcastListPorts){
        // first we need local lock so that other threads on the same node can't increment rn
         while(!localLock.compareAndSet(false, true)){
                try {
                    // busy wait
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
         }

        // increment  my request number
        incrementRequestNumber();

        // broadcast my request for token
        broadcastTokenRequest(broadcastListPorts);
        usesSuzukiToken.set(true);

        // wait until I get the token
        while (!hasSuzukiToken.get()){
            try {
                // busy wait
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void distributedUnlock(){
        // update LN in token
        token.getLn().set(nodeId, rn.get(nodeId));

        // add requests to queue if they are not already in it

        for(int i = 0; i < rn.size(); i++){
            Integer iport = getPortForNode(i);
            if(rn.get(i) == token.getLn().get(i) + 1 && !token.getQueue().contains(iport) && iport != -1){
                token.getQueue().add(iport);
            }
        }

        // if someone is waiting in the queue for node, send token
        possibleSendingToken();

        usesSuzukiToken.set(false);

        // release local lock
        localLock.set(false);
    }

    public void possibleSendingToken() {
        if(!token.getQueue().isEmpty()){
            int receiverPort = token.getQueue().poll();
            Message tokenMessage = new SendSuzukiTokenMessage(AppConfig.myServentInfo.getListenerPort(), receiverPort, token);
            MessageUtil.sendMessage(tokenMessage);
            hasSuzukiToken.set(false);
            usesSuzukiToken.set(false);
            token = null;
        }
    }

    public void incrementRequestNumber(){
        rn.set(nodeId, rn.get(nodeId) + 1);
    }

    public void broadcastTokenRequest(List<Integer> broadcastListPorts){
        for(Integer receiverPort : broadcastListPorts) {
            Message requestMessage = new RequestSuzukiTokenMessage(AppConfig.myServentInfo.getListenerPort(), receiverPort, rn.get(nodeId));
            MessageUtil.sendMessage(requestMessage);
        }
    }

    // sets token and hasToken to true
    public void setToken(SuzukiKasamiToken token) {
        this.token = token;
        this.hasSuzukiToken.set(true);
    }

    private Integer getPortForNode(int nodeId){
        for (ServentInfo nodeInfo : AppConfig.chordState.getAllNodeInfo()) {
            if (nodeInfo.getChordId() == nodeId)
                return nodeInfo.getListenerPort();
        }
        // not all nodes will be servents
//        AppConfig.timestampedErrorPrint("Node with id " + nodeId + " not found in allNodeInfo");
        return -1;
    }

    public CopyOnWriteArrayList<Integer> getRn() {
        return rn;
    }

    public AtomicBoolean getHasSuzukiToken() {
        return hasSuzukiToken;
    }

    public void setHasSuzukiToken(AtomicBoolean hasSuzukiToken) {
        this.hasSuzukiToken = hasSuzukiToken;
    }

    public AtomicBoolean getUsesSuzukiToken() {
        return usesSuzukiToken;
    }

    public void setUsesSuzukiToken(AtomicBoolean usesSuzukiToken) {
        this.usesSuzukiToken = usesSuzukiToken;
    }

    public SuzukiKasamiToken getToken() {
        return token;
    }
}
