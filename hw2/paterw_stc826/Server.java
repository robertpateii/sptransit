
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {

    protected final ArrayList<InetSocketAddress> serverAddresses; // used by heartbeat and lamport
    protected final InetSocketAddress myAddress; // ??do we need this or an index?
    private final int myID;
    protected final Mutex mutex;
    protected final ReservationMgr resMgr;
    private final Heartbeat hBeat;
    private final Recovery recovery;
    protected boolean acceptingClientConnections;
    private final ArrayList<InetSocketAddress> inputServers;
    /* timeout here can't be the same as the clients...
        client waits 100ms for reply from one server
        server waits 100ms for every other server
        if there are 2 servers and 1 times out in this for loop then 100 ms have passed
        client will have waited 100ms too and try a different server
        said different server is the dead one
        client will try 1st server again
        i guess everyone will keep looping forever until servers stop dying in this for loop
        ASSUMPTION: servers won't die in this for loop on every loop forever
    */
    // waiting 100ms will cause client to disconnect but that's ok because above logic
    private int messageServerTimeout = 100;

    public static void main(String[] args) {
        System.out.println("Scanning server input files.");
        Scanner sc = new Scanner(System.in);
        int id = sc.nextInt();
        int numServer = sc.nextInt();
        int numSeats = sc.nextInt();
        System.out.printf("My ID: %d; numServers: %d; numSeats: %d\n", id, numServer, numSeats);
        ArrayList<InetSocketAddress> inputServers = new ArrayList<>();
        for (int i = 0; i < numServer; i++) {
            String temp = sc.next();
            //skip my address from the list
            if (i == id - 1) {
                System.out.println("My Address: " + temp);
            } else {
                System.out.println("Other Address: " + temp);
            }
            int spacerIndex = temp.indexOf(":");
            String host = temp.substring(0, spacerIndex);
            int port = Integer.parseInt(temp.substring(spacerIndex + 1));
            inputServers.add(new InetSocketAddress(host, port));
        }

        System.out.println("Creating Reservation Server ...");
        Server thisServer = new Server(id, numSeats, inputServers);
    }

    public Server(int id, int numSeats, ArrayList<InetSocketAddress> inputServers) {
        myID = id;
        this.inputServers = inputServers;
        serverAddresses = new ArrayList<>();
        myAddress = inputServers.get(myID - 1); // Server ID is 1-indexed
        int myPort = myAddress.getPort();
        System.out.println("Starting heartbeat");
        hBeat = new Heartbeat(inputServers, this); // prune and setup hbeat
        System.out.println("Attempting recovery");
        recovery = new Recovery(this);
        if (recovery.thisIsOnlyServer) {
            System.out.println("This is the only server. Setting up ReservationMgr and Mutex solo.");
            resMgr = new ReservationMgr(numSeats);
            mutex = new Mutex(myID, inputServers.size(), resMgr, this);
        } else {
            System.out.println("Successful remote recovery, setting up ReservationMgr and Mutex");
            resMgr = new ReservationMgr(recovery.seatList);
            mutex = new Mutex(myID, inputServers.size(), resMgr, recovery.pendingQueue, this);
        }
        // we're full recovered now, begin accepting clients
        System.out.println("Opening connections for servers from ID: " + myID
                + " and port: " + myPort);

        acceptingClientConnections = true; // this is also used when we're in CS so don't get rid of it
        OpenConnection(myPort);
    }

    private boolean isServer(Socket pipe) {
        // TODO: test that the implicit type conversion works
        InetSocketAddress remote = (InetSocketAddress) pipe.getRemoteSocketAddress();
        return inputServers.contains(remote);
    }

    private void OpenConnection(int port) {
        ServerSocket listener;
        Socket pipe;
        try {
            System.out.println("Waiting for connection..");
            listener = new ServerSocket(port);
            while ((pipe = listener.accept()) != null) {
                System.out.println("Got Connection " + pipe.getRemoteSocketAddress().toString());
                handleConnection(pipe);
                System.out.println("OpenConnection: loop on listener bottom");
                // don't close in case needed by heartbeat/recovery/etc
            }
            listener.close(); // redundant since while is forever?
        } catch (IOException ex) {
            System.err.print(ex);
        }
    }

    private void handleConnection(Socket pipe) {
        //read message
        /* Read and parse the message. ASSUMPTIONS:
            1. one line for one message
            2. first word is the command
            3. Command separated by space from rest of message
         */
        try {
            System.out.println("Entering Handle Connection"); 
            InputStream inputStream = pipe.getInputStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream));
            //assuming single line messages for everything except recovery
            String message = in.readLine();

            //get the command
            String command = message.split(" ")[0];
            System.out.println("**Recieved Command : " + command);

            switch (command) {
                //client commands
                case "reserve":
                case "bookSeat":
                case "search":
                case "delete":
                    /* sam tuesday stuff
                    if(clientRequestQueue.size()==0)
                        requestCriticalSection(message,pipe);
                    //keep client message to ensure we handle all the requests
                    clientRequestQueue.add(message);
                    clientSockets.add(pipe);
                     */
                    if (!acceptingClientConnections) {
                        pipe.close();
                    }
                    System.out.println("HandleConnection: pre-requestCS");
                    mutex.RequestCS(message, pipe);
                    System.out.println("HandleConnection: post-requestCS");
                    break;
                //server commands
                case "requestCS":
                    System.out.println("HandleConnection: pre-OnReqRequest");
                    mutex.OnReceiveRequest(message, pipe);
                    System.out.println("HandleConnection: post-OnReqRequest");
                    break;
                case "ack":
                    mutex.OnReceiveAck();
                    break;
                case "release":
                    mutex.OnReceiveRelease(message);
                    break;
                case "heartbeat":
                    hBeat.onRecieveHeartbeat(pipe);
                    break;
                case "connect":
                    recovery.OnReceiveConnect(message, pipe);
                    break;
                default:
                    System.err.println("Invalid command type: " + command);
            }
        } catch (IOException e) {
            System.err.print(e);
        }

        // add client to client list
        // add server to server list
        // handle the content of the connection?
        // handleClient if it's a client
        // do something else if it's a server message, heart beat, ack, etc
        // pipe.close() at some point??!
    }

    protected void messageServer(String msg, InetSocketAddress addy) {
            String addyStr = addy.toString();
            try {
                Socket s = new Socket(addy.getHostString(), addy.getPort());
                System.out.println("Sending " + msg + "to" + addyStr);
                messageServer(s, msg);
            } catch (IOException ex) {
                System.out.println("Failed server msg: " + msg + " to " + addyStr);
                serverAddresses.remove(addy);
            }
    }

    private void messageServer(Socket s, String msg) throws IOException {
        System.out.println("Enter: messageServer -socket-message");
        s.setSoTimeout(messageServerTimeout);
        DataOutputStream pout = new DataOutputStream(s.getOutputStream());
        pout.writeBytes(msg + '\n');
        pout.flush();
        pout.close();
        s.close();
        System.out.println("Exit: messageServer -socket-message");
    }

    protected void messageAllServers(String msg) {
        System.out.println("Sending server message to all " + serverAddresses.size() + " servers");
        LinkedList<Integer> deadServers = new LinkedList<>();
        for (int i = 0; i < serverAddresses.size(); i++) {
            InetSocketAddress addy = serverAddresses.get(i);
            String addyStr = addy.getHostString() + " and port " + addy.getPort();
            if (addy.getHostString() == null) {
                throw new RuntimeException("WHY IS ADDRESS NULL");
            }
            try {
                System.out.println("Trying send to '" + msg + "' to " + addyStr);
                Socket s = new Socket("127.0.0.1", addy.getPort());
                if (!s.isConnected()) {
                    throw new RuntimeException("Socket didn't connect or died or ? " + s.getInetAddress());
                }
                messageServer(s, msg);
            } catch (IOException ex) {
                System.out.println(ex);
                System.out.println("Failed server msg: " + msg + " to " + addyStr);
                deadServers.add(i);
            }
        }
        for (Integer index : deadServers) {
            // handling heartbeat here, also copied this into recovery
            System.out.println("Dead servers found, removing port " + serverAddresses.get(index.intValue()).getPort());
            serverAddresses.remove(index.intValue());
        }
        if (serverAddresses.isEmpty() && deadServers.size() > 0) {
            System.out.println("Tried to message all servers but they died. Acquire my own CS if needed.");
            if (msg.contains("requestCS")) {
                System.out.println("Yeah going into CS since my messages timed out to all others.");
                mutex.EnterCriticalSection();
            }
        }
        System.out.println("Done sending message to all servers i guess i'll just wait for next connection.");
    }
}
