
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.net.*;
// based on https://github.com/vijaygarg1/EE-382N-Distributed-Systems-Option-3/blob/master/Distributed-Algorithms/mutex/LamportMutex.java

public class Mutex {

    private LamportClock c;
    protected PriorityQueue<CSRequest> q; // timestamp, and command
    private int numAcks;
    private int myId;
    private Server parent;
    private ReservationMgr _resManager;

    private Socket _clientSocket;
    private String _clientCommand;

    public Mutex(int serverId, int expectedServers, ReservationMgr resMgr, Server parent) {
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
        this.parent = parent;
        _resManager = resMgr;
    }

    public Mutex(int serverId, int expectedServers,
                 ReservationMgr resMgr, Queue<CSRequest> existing, Server parent) {

        this(serverId, expectedServers, resMgr, parent);
        for (CSRequest req : existing) {
            q.offer(req);
        }
    }

    public void RequestCS(String command, Socket pipe) {
        System.out.println("Requesting Critical Section ...");
        c.tick();
        parent.acceptingClientConnections = false;

        Timestamp ts = new Timestamp(c.getValue(), myId);
        CSRequest req = new CSRequest(ts, command);
        _clientCommand = command;
        _clientSocket = pipe;


        // req includes timestamp and command and client socket/pipe*/
        if(parent.serverAddresses.size()> 0) {
            parent.messageAllServers("requestCS " + ts);
            q.offer(req);
            numAcks = 0;
        }
        else {
            //TODO : this might not work when the server is recovering
            System.out.println("Entering Critical Section since there are no additional servers");
            EnterCriticalSection();
        }
    }

    void OnReceiveRequest(String message, Socket pipe) {
        CSRequest req = CSRequest.Parse(message);
        c.receiveAction(req.get_timeStamp().getPid(),req.get_timeStamp().getLogicalClock());
        q.add(new CSRequest(message));
        parent.messageServer("ack",(InetSocketAddress) pipe.getLocalSocketAddress());
    }

    void OnReceiveAck() {
        dumpQueue();
        numAcks++;
        if(numAcks == parent.serverAddresses.size() && q.peek().get_timeStamp().getPid() == myId)
        {
            System.out.println("This server enters critical section from ACK");
            if (q.peek().get_timeStamp().getPid() != myId) {
                throw new RuntimeException("Got all acks but my message is not at top of queue.");
            }
            EnterCriticalSection();
        }
    }

    void OnReceiveRelease(String command) {
        // REMOVE TIP AND UPDATE RES MANAGER
        dumpQueue();
        int src = Integer.parseInt(command.split(" ")[1]);
        System.out.println("Got release from server position id " + src);
        CSRequest releasedReq = q.remove();
        int tsId = releasedReq.get_timeStamp().getPid();
        System.out.println("Running " + releasedReq.get_command() + " from timestamp id" + tsId);
        if (src != tsId) {
            throw new RuntimeException("Got release from " + src + " but released message from " + tsId);
        }

        String result = _resManager.HandleCommand(_clientCommand);

        // server addresses is already n-1 because it doesn't include this server
        System.out.println("Acks so far " + numAcks + " expected acks " + parent.serverAddresses.size());
        if(numAcks == parent.serverAddresses.size())
        {
            System.out.println("This server enters critical section from release");
            if (q.peek().get_timeStamp().getPid() != myId) {
                throw new RuntimeException("Got all acks but my message is not at top of queue.");
            }
            EnterCriticalSection();
        }
    }

    public void Release() {
        /*
		q.remove();
		sendMsg(neighbors, "release", c.getValue());

         */
        parent.acceptingClientConnections = true;
        if(q.size()>0) {
            q.remove();
            parent.messageAllServers("release " + myId);
        }
    }

    public void EnterCriticalSection() {
        // execute the top of the q, my command?
        String result = _resManager.HandleCommand(_clientCommand);
        // pipe stuff
        PrintWriter out
                = null;
        try {
            System.out.println("got a response "+result);
            System.out.println("socket is connected " + _clientSocket.isConnected() + " to " + _clientSocket.getRemoteSocketAddress());
            out = new PrintWriter(_clientSocket.getOutputStream(), true);
            out.write(result);
            out.flush();
            System.out.println("wrote and flushed");
            // don't close the socket? client is getting error before handling the input. let them close?
            // don't null out either, only used here and request CS where it gets set
            Release();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void dumpQueue()
    {
        System.out.println("**********************");
        Iterator<CSRequest> it =  q.iterator();
        while (it.hasNext()){
            System.out.println(it.next().get_timeStamp());
        }
        System.out.println("**********************");
    }
}
