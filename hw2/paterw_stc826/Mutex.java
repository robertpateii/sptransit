import java.util.*;
import java.net.*;

public class Mutex {
	private LamportClock c;
    private PriorityQueue<CSRequest> q; // timestamp, socket, and command
    private int numAcks;
    private int myId;

    public Mutex(int serverId, int expectedServers, ReservationMgr resMgr) {
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

    public Mutex (int serverId, int expectedServers, 
            ReservationMgr resMgr, Queue<CSRequest> existing) {

        this(serverId, expectedServers, resMgr);
        for (CSRequest req : existing) {
            q.offer(req);
        }
    }

	public void RequestCS(String command, Socket pipe) {
		c.tick();
        System.out.println("Requesting Critical Section ...");
        //sends requests to all the servers in the list
        /* sam tuesday stuff:
        int ts = getLogicalClock(0);
        csRquestQueue.add(new CSRequest(this.myID,ts));
        for(int i = 0;i<servers.size();i++)
        {
            //don't send to myself
            if(i==this.myID-1) continue;
            sendMessage("requestCS "+this.myID+ " "+ts+ " "+  message,servers.get(i).getAddress());
        }
        numAcks = 0;
        */
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


    private void onReceiveRequestSamTuesday(String message) {
        String[] args = message.split(" ");

        int senderId = Integer.parseInt(args[1]);
        int ts = Integer.parseInt(args[2]);
        csRquestQueue.add(new CSRequest(senderId,ts));
        sendMessage("Ack ",getServerById(senderId).getAddress());
    }

    void OnReceiveAck() {
			numAcks++;
        // numacks += 1;
        // if numacks = N - 1 and my request is smallest in q {
            // enterCriticalSection
            // }
    }

    private void onReceiveAckSamTuesday(String message) {
        // numacks += 1;
        // if numacks = N - 1 and my request is smallest in q {
            // enterCriticalSection
            // }
        numAcks+=1;
        if(numAcks==servers.size()-1 && getTheSmallestRequest().get_pid() == myID)
            enterCriticalSection();
    }


    void OnReceiveRelease() {
        /*
        Iterator<Timestamp> it =  q.iterator();			    
        while (it.hasNext()){
            if (it.next().getPid() == src) it.remove();
        }
        */
    }

    private void onReceiveReleaseSamTuesday(String message) {
        String[] args = message.split(" ");
        int senderId = Integer.parseInt(args[1]);

        int index = -1;
        for(int i = 0;i<csRquestQueue.size();i++)
            if(csRquestQueue.get(i).get_pid()==senderId)
            {
                index = i;
                break;
            }

        csRquestQueue.remove(index);
    }

    public void Release() {
        /*
		q.remove();
		sendMsg(neighbors, "release", c.getValue());

        */

    }

    private void releaseSamTuesday() {
       for(int i = 0;i<servers.size();i++)
       {
           if(servers.get(i).getId()==myID) continue;
           sendMessage("release",servers.get(i).getAddress());
       }
    }

    public void EnterCriticalSection() {
        // execute the top of the q, my command?
        Release();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void enterCriticalSectionSamTuesday() {
        //process the first client request that was queued up
        //this is not fair
        try
        {
        for(int i =0;i<clientRequestQueue.size();i++) {
            String response = handleClientCommand(clientRequestQueue.get(i));
            PrintWriter out
                    = new PrintWriter(clientSockets.get(i).getOutputStream(), true);
            out.write(response);
            out.flush();
            clientSockets.get(i).close();
        }

        clientRequestQueue=new ArrayList<>();
        clientSockets=new ArrayList<>();

        release();

        } catch (IOException ex) {
            System.err.print(ex);
        }
    }
}
