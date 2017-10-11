import java.util.*;
import java.net.*;

public class Mutex {
	private LamportClock c;
    private Queue<CSRequest> q; // timestamp, socket, and command
    private int numAcks;
    private int myId;

    public Mutex(int serverId, int expectedServers) {
        c = new LamportClock();
		q = new PriorityQueue<>(
                expectedServers,
                new Comparator<CSRequest>() {
                    public int compare(CSRequest a, CSRequest b) {
                        return Timestamp.compare(
                                a.get_timeStamp(),
                                b.get_timeStamp()
                        );
                    }
                }
        );
        numAcks = 0;
        myId = serverId;
    }

    public Mutex (int serverId, int expectedServers, PriorityQueue<CSRequest> existing) {
        c = new LamportClock();
        q = existing;
        numAcks = 0;
        myId = serverId;
    }

	public void RequestCS(String command, Socket pipe) {
		c.tick();
        Timestamp ts = new Timestamp(c.getValue(), myId);
        CSRequest req = new CSRequest(pipe, ts, command);
        /* TODO:send req to all other servers
        // req includes timestamp and command and client socket/pipe
        for(int i = 0;i<servers.size();i++)
        {
            sendMessage(message + " "+(logicalClock),servers.get(i));
        }
        */
		q.add(req);
		numAcks = 0;
        /*
        public synchronized void requestCS() {
            c.tick();
            q.add(new Timestamp(c.getValue(), myId));
            sendMsg(neighbors, "request", c.getValue());
            numAcks = 0;
            while ((q.peek().pid != myId) || (numAcks < n-1))
                myWait();
        } */
	}

    void OnReceiveRequest(String message, Socket pipe) {
        CSRequest req = CSRequest.Parse(message, pipe);
        /*
		int ts = req.get_timeStamp();
		c.receiveAction(src, timeStamp);
        q.add(new Timestamp(timeStamp, src));
        sendMsg(src, "ack",c.getValue());
        */
    }

    void OnReceiveAck() {
			numAcks++;
        // numacks += 1;
        // if numacks = N - 1 and my request is smallest in q {
            // enterCriticalSection
            // }
    }


    void OnReceiveRelease() {
        /*
        Iterator<Timestamp> it =  q.iterator();			    
        while (it.hasNext()){
            if (it.next().getPid() == src) it.remove();
        }
        */
    }

    public void Release() {
        /*
		q.remove();
		sendMsg(neighbors, "release", c.getValue());

        */

    }

    public void EnterCriticalSection() {
        // execute the top of the q, my command?
        Release();
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
