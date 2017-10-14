
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {

    protected final ArrayList<InetSocketAddress> serverAddresses; // used by heartbeat and lamport
    protected final InetSocketAddress myAddress; // ??do we need this or an index?
    private final int myID;
    private final Mutex mutex;
    private final ReservationMgr resMgr;
    private final Heartbeat hBeat;
    private final Recovery recovery;
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
        serverAddresses = new ArrayList<>();
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
                    mutex.OnReceiveRelease(message);
                    break;
                case "heartbeat":
                    hBeat.onRecieveHeartbeat(pipe);
                    break;
                case "connect":
                    recovery.OnReceiveConnect();
                    break;
                case "recover":
                    // read the serialized queue and seat table somehow
                    recovery.OnReceiveRecoveryState();
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
                Socket s = new Socket(addy.getAddress(), addy.getPort());
                System.out.println("Sending " + msg + "to" + addyStr);
                messageServer(s, msg);
            } catch (IOException ex) {
                System.out.println("Failed server: " + addyStr);
                serverAddresses.remove(addy);
            }
    }

    private void messageServer(Socket s, String msg) throws IOException {
        s.setSoTimeout(100);
        DataOutputStream pout = new DataOutputStream(s.getOutputStream());
        pout.writeBytes(msg + '\n');
        pout.flush();
        pout.close();
    }

    protected void messageAllServers(String msg) {
        System.out.println("Sending server message to all");
        LinkedList<Integer> deadServers = new LinkedList<>();
        for (int i = 0; i < serverAddresses.size(); i++) {
            InetSocketAddress addy = serverAddresses.get(i);
            String addyStr = addy.toString();
            try {
                Socket s = new Socket(addy.getAddress(), addy.getPort());
                System.out.println("Sending " + msg + "to" + addyStr);
                messageServer(s, msg);
            } catch (IOException ex) {
                System.out.println("Failed server: " + addyStr);
                deadServers.add(i);
            }
        }
        for (Integer index : deadServers) {
            serverAddresses.remove(index.intValue());
        }
    }
}
