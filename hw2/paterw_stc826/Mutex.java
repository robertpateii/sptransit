
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.net.*;
// based on https://github.com/vijaygarg1/EE-382N-Distributed-Systems-Option-3/blob/master/Distributed-Algorithms/mutex/LamportMutex.java

public class Mutex {

    private LamportClock c;
    private PriorityQueue<CSRequest> q; // timestamp, socket, and command
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
        CSRequest req = new CSRequest(pipe, ts, command);
        /* TODO:send req to all other servers
        // req includes timestamp and command and client socket/pipe*/
        parent.messageAllServers("requestCS "+ts);
        _clientCommand = command;
        _clientSocket = pipe;

        q.offer(req);
        numAcks = 0;
    }

    void OnReceiveRequest(String message, Socket pipe) {
        CSRequest req = CSRequest.Parse(message, pipe);
        c.receiveAction(req.get_timeStamp().getPid(),req.get_timeStamp().getLogicalClock());
        q.add(new CSRequest(message));
        parent.messageServer("ack",(InetSocketAddress) pipe.getLocalSocketAddress());
    }

    void OnReceiveAck() {
        dumpQueue();
        numAcks++;
        if(numAcks == parent.serverAddresses.size()-1 && q.peek().get_timeStamp().getPid() == myId)
        {
            System.out.println("heeeey I got the critical section from acknowledge");
            EnterCriticalSection();
        }
    }

    void OnReceiveRelease(String command) {
        dumpQueue();
        int src = Integer.parseInt(command.split(" ")[1]);
        Iterator<CSRequest> it =  q.iterator();
        while (it.hasNext()){
            if (it.next().get_timeStamp().getPid() == src) it.remove();
        }

        if(numAcks == parent.serverAddresses.size()-1 && q.peek().get_timeStamp().getPid() == myId)
        {
            System.out.println("heeeey I got the critical section from release");
            EnterCriticalSection();
        }
    }

    public void Release() {
        /*
		q.remove();
		sendMsg(neighbors, "release", c.getValue());

         */
        parent.acceptingClientConnections = true;
        q.remove();

        parent.messageAllServers("release "+myId);
    }

    /*
    private void releaseSamTuesday() {
       for(int i = 0;i<servers.size();i++)
       {
           if(servers.get(i).getId()==myID) continue;
           sendMessage("release",servers.get(i).getAddress());
       }
    }
     */
    public void EnterCriticalSection() {
        // execute the top of the q, my command?
        String result = _resManager.HandleCommand(_clientCommand);
        // pipe stuff
        PrintWriter out
                = null;
        try {
            System.out.println("got a response "+result);
            out = new PrintWriter(_clientSocket.getOutputStream(), true);
            out.write(result);
            out.flush();

            _clientSocket.close();
            _clientSocket = null;
            _clientCommand = null;
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
