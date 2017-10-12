
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {

    private ArrayList<InetSocketAddress> servers;
    protected final HashSet<Socket> serverSockets; // used by heartbeat and lamport
    protected final InetSocketAddress myAddress; // ??do we need this or an index?
    private final int myID;
    private Mutex mutex;
    private ReservationMgr resMgr;
    private Heartbeat hBeat;
    private Recovery recovery;
    protected boolean acceptingClientConnections;
    private final ArrayList<InetSocketAddress> inputServers;

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
        serverSockets = new HashSet<>();
        myAddress = inputServers.get(myID - 1); // Server ID is 1-indexed
        int myPort = myAddress.getPort();
        System.out.println("Opening connections for servers from ID: " + myID
                + " and port: " + myPort);
        System.out.println("Starting heartbeat");
        hBeat = new Heartbeat(inputServers, this); // prune and setup hbeat
        System.out.println("Attempting recovery");
        recovery = new Recovery(this);
        if (recovery.wasSuccessful) {
            System.out.println("Successful recovery, setting up ReservationMgr and Mutex");
            resMgr = new ReservationMgr(recovery.seatList);
            mutex = new Mutex(myID, inputServers.size(), resMgr, recovery.pendingQueue, this);
        } else {
            System.out.println("Recovery failed! Setting up ReservationMgr and Mutex");
            resMgr = new ReservationMgr(numSeats);
            mutex = new Mutex(myID, inputServers.size(), resMgr, this);
        }
        // we're full recovered now, begin accepting clients
        System.out.println("Opening connections to clients");
        acceptingClientConnections = true;
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
                //todo make this multi threaded
                if (acceptingClientConnections) {
                    handleConnection(pipe);
                } else if (isServer(pipe)) {
                    handleConnection(pipe);
                } else {
                    // pipe is a client, but we haven't finished recovery yet
                    // don't read its input, close it so it tries another server
                    pipe.close();
                }
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
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(pipe.getInputStream()));
            //assuming single line messages for everything except recovery
            String message = in.readLine();

            //get the command
            String command = message.split(" ")[0];
            System.out.println("Recieved Command : " + command);

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
                    mutex.RequestCS(message, pipe);
                    break;
                //server commands
                case "requestCS":
                    mutex.OnReceiveRequest(message, pipe);
                    break;
                case "ack":
                    mutex.OnReceiveAck();
                    break;
                case "release":
                    mutex.OnReceiveRelease();
                    break;
                case "heartbeat":
                    hBeat.onRecieveHeartbeat(pipe);
                case "connect":
                    recovery.OnReceiveConnect();
                case "recover":
                    // read the serialized queue and seat table somehow
                    recovery.OnReceiveRecoveryState();
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

    // for our clients
    private void handleCommand(String command, Socket pipe) throws IOException {
        String response = resMgr.HandleCommand(command);
        // pipe stuff
        PrintWriter out
                = new PrintWriter(pipe.getOutputStream(), true);
        out.write(response);
        out.flush();
        pipe.close();
    }

    protected void sendMessage(String msg, Socket server) {
    }

    protected void sendMessage(String msg, InetSocketAddress inetSocketAddress) {
        // do we need this?Should already have socket in serverSockets or...
        // maybe we need it for clients?If it stays open... we should have like
        // a list of clients?
        Socket server = new Socket();
        try {
            server.connect(inetSocketAddress);
            DataOutputStream pout
                    = new DataOutputStream(server.getOutputStream());
            pout.writeBytes(msg + '\n');
            pout.flush();
            server.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    // same tuesday night stuff, was sendMessage();
    private String sendAndReceiveMessage(String message, InetSocketAddress inetSocketAddress) {
        try {
            Socket server = new Socket();
            server.connect(inetSocketAddress);

            BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
            DataOutputStream pout = new DataOutputStream(server.getOutputStream());

            pout.writeBytes(message + '\n');
            pout.flush();

            String retValue = din.readLine(); // scanner next time
            server.close();

            return retValue;
        } catch (IOException e) {
            System.err.print(e);
            return null;

        }
    }

}
