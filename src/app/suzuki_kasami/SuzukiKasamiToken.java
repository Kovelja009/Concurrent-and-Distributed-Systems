package app.suzuki_kasami;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


// needs to be Serializable because we are sending it over the network

public class SuzukiKasamiToken implements java.io.Serializable {
    // lock number
    private List<Integer> ln;
    private Queue<Integer> queue;

    public SuzukiKasamiToken(int numNodes) {
        // initialize lock numbers with 0
        ln = new ArrayList<>();
        for (int i = 0; i < numNodes; i++)
            ln.add(0);

        queue = new LinkedList<>();
    }

    public List<Integer> getLn() {
        return ln;
    }

    public void setLn(List<Integer> ln) {
        this.ln = ln;
    }

    public Queue<Integer> getQueue() {
        return queue;
    }

    public void setQueue(Queue<Integer> queue) {
        this.queue = queue;
    }
}
