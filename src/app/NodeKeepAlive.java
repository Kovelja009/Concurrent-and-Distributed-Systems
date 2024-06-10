package app;

public class NodeKeepAlive {
    // 0 - alive, 1 - suspicious, 2 - dead
    private volatile int status;

    /* timestamp of the last ping message (resets on every pong message)
    *  we have weak and strong limit
    *  if the timestamp is older than the weak limit, we set it to suspicious
    *  and send broadcast message to check whether the node is alive (if at least one node says it is alive,
    * we set it to alive and reset the timestamp after which we again send ping message)
    * if the timestamp is older than the strong limit, we set it to dead and trigger reorganization of the network
    */
    private volatile long timestamp;


    /*
    *  KeepAlive will send broadcast message after the timestamp is older than the weak limit and set doneBroadcast to true.
    *  If listener gets the message that the node is alive (either by node or others in the system) it will reset the timestamp
    *  and set status to alive, and doneBroadcast to false
    */
    private volatile boolean doneBroadcast;

    /*
    *  Successor timestamp is used to check whether the successor is alive
    *  because we are sending him message to update the network
    */
    private volatile long successorTimestamp;

    private volatile boolean successorAlive = true;

    public int getSuccessorPort() {
        return successorPort;
    }

    public void setSuccessorPort(int successorPort) {
        this.successorPort = successorPort;
    }

    public long getSuccessorTimestamp() {
        return successorTimestamp;
    }

    public void setSuccessorTimestamp(long successorTimestamp) {
        this.successorTimestamp = successorTimestamp;
    }

    /*
    * successorPort is used to send message to the successor
    * if failed, reset timestamp and change successorPort
    */
    private volatile int successorPort;

    public NodeKeepAlive() {
        this.status = 0;
        this.timestamp = System.currentTimeMillis();
        this.doneBroadcast = false;
        this.successorTimestamp = System.currentTimeMillis();
        this.successorPort = -1;
        this.successorAlive = false;
    }

    public int getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isDoneBroadcast() {
        return doneBroadcast;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDoneBroadcast(boolean doneBroadcast) {
        this.doneBroadcast = doneBroadcast;
    }

    public void resetTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public void resetSuccessorTimestamp() {
        this.successorTimestamp = System.currentTimeMillis();
    }

    public boolean isSuccessorAlive() {
        return successorAlive;
    }

    public void setSuccessorAlive(boolean successorAlive) {
        this.successorAlive = successorAlive;
    }
}
