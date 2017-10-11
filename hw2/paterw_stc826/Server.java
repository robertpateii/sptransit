
package reservation;

import java.util.ArrayList;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Server {

    private ArrayList<ReservationServer> servers; // used by recovery/heartbeat?
    private ArrayList<CSRequest> csRquestQueue;
    private ArrayList<String> clientRequestQueue;
    private ArrayList<Socket> clientSockets;
    private int logicalClock = 0;
    private int numAcks = 0;
    private int myID=0;

    /*LAMPORTS MUTEX */
    /* data structure for queue:
        int ts; // logical clock's timestamp
        int pid; // process id, used to break timestamp ties? see https://en.wikipedia.org/wiki/Lamport_timestamps#Considerations
        String command;
        Socket pipe;
    */
    // numAcks
    // logical clock stuff?! don't forget about his implementations on github

    public static void main(String[] args) {
        System.out.println("Starting Reservation Server ...");
        Server thisServer = new Server();

        Scanner sc = new Scanner(System.in);
        thisServer.myID = sc.nextInt();
        int numServer = sc.nextInt();
        int numSeat = sc.nextInt();

        System.out.println("ID:"+thisServer.myID);

        //init seats
        ReservationManager.Initialize(numSeat);

        //init servers
        thisServer.servers = new ArrayList<>();
        for (int i = 0; i < numServer; i++) {
            String temp = sc.next();
            int spacerIndex = temp.indexOf(":");
            String host = temp.substring(0, spacerIndex);
            int port = Integer.parseInt(temp.substring(spacerIndex + 1));
            thisServer.servers.add(new ReservationServer(i+1, new InetSocketAddress(host, port),true));
        }

        //init the critical section queue
        thisServer.csRquestQueue =new ArrayList<CSRequest>();
        thisServer.clientRequestQueue = new ArrayList<String>();
        thisServer.clientSockets = new ArrayList<>();

        //start the server
        thisServer.go();
    }

    private void go() {
        //StartHeartbeat(); // heartbeat prunes dead servers
        //RecoverState(); // if no other servers up, use empty seat array
        OpenConnection(getServerById(myID).getAddress().getPort());
    }

    private void StartHeartbeat() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*	1. Start heartbeat to all servers in list
            2. Remove dead servers
            assume 1 and 2 is continuous!
            prevents us from sending other messages to dead servers
        */
    }

    private void RecoverState() {
        throw new UnsupportedOperationException("Not supported yet.");
        // if no other server, proceed w/ empty seat array
    /*  3. send Connect(inetaddress/port) to all servers;
        4. Other servers add you to their list
        5. Other servers send their seat table and queue
        6. Wait for seat table and queue from all living servers
        Use seat table/queue with the latest timestamp, beats problem A
        ssee methods we made for this
    */
    }

    private void OpenConnection(int port) {
        ServerSocket listener;
        Socket pipe;
        System.out.println("Waiting for connection..");
        try {
            listener = new ServerSocket(port);
            while ((pipe = listener.accept()) != null) {
                //todo make this multi threaded
                handleConnection(pipe);
            }
            listener.close(); // not needed since while is forever?
        } catch (IOException ex) {
            System.err.print(ex);
        }
    }

    private void handleConnection(Socket pipe) {
        //read message
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(pipe.getInputStream()));
            //assuming single line messages
            String message = in.readLine();

            //get the command
            String command = message.split(" ")[0];

            System.out.println("Recieved Command : "+command);

            switch(command)
            {
                //client commands
                case "reserve":
                case "bookSeat":
                case "search":
                case "delete":
                    if(clientRequestQueue.size()==0)
                        requestCriticalSection(message,pipe);
                    //keep client message to ensure we handle all the requests
                    clientRequestQueue.add(message);
                    clientSockets.add(pipe);
                    break;
                //server commands
                case "requestCS":
                    onReceiveRequest(message);
                    break;
                case "ack":
                    onReceiveAck(message);
                    break;
                case "release":
                    onReceiveRelease(message);
                    break;
                //todo add code to handle the recovery messages
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

    // for changes from other servers
    private String handleClientCommand(String command) {
       return ReservationManager.HandleCommand(command);
    }
    
    // for our clients
    private void handleCommand(String command, Socket pipe) throws IOException {
        String response = handleClientCommand(command);
        // pipe stuff
        PrintWriter out
                = new PrintWriter(pipe.getOutputStream(), true);
        out.write(response);
        out.flush();
        pipe.close();
    }

    private void requestCriticalSection(String message, Socket pipe) {
        System.out.println("Requesting Critical Section ...");
        //sends requests to all the servers in the list
        int ts = getLogicalClock(0);
        csRquestQueue.add(new CSRequest(this.myID,ts));
        for(int i = 0;i<servers.size();i++)
        {
            //don't send to myself
            if(i==this.myID-1) continue;
            sendMessage("requestCS "+this.myID+ " "+ts+ " "+  message,servers.get(i).getAddress());
        }
        numAcks = 0;
    }

    private int getLogicalClock(int requestLogicalClock)
    {
        logicalClock++;
        if(logicalClock<requestLogicalClock)
            logicalClock = requestLogicalClock+1;

        return logicalClock;
    }

    private String sendMessage(String message, InetSocketAddress inetSocketAddress) {
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

    private void onReceiveRequest(String message) {
        String[] args = message.split(" ");

        int senderId = Integer.parseInt(args[1]);
        int ts = Integer.parseInt(args[2]);
        csRquestQueue.add(new CSRequest(senderId,ts));
        sendMessage("Ack ",getServerById(senderId).getAddress());
    }

    private void onReceiveAck(String message) {
        // numacks += 1;
        // if numacks = N - 1 and my request is smallest in q {
            // enterCriticalSection
            // }
        numAcks+=1;
        if(numAcks==servers.size()-1 && getTheSmallestRequest().get_pid() == myID)
            enterCriticalSection();
    }

    private void onReceiveRelease(String message) {
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

    private void enterCriticalSection() {
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

    private void release() {
       for(int i = 0;i<servers.size();i++)
       {
           if(servers.get(i).getId()==myID) continue;
           sendMessage("release",servers.get(i).getAddress());
       }
    }

    private void onRecieveHeartbeat() {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void sendConnect() {
        // used when coming up from crash to get into others' server list
    }

    // servers send connect when coming back from crash
    private void onRecieveConnect() {

    }
    
    // this is what servers send back after receiving your connect message
    private void onRecieveRecoveryState() {
        // block until got states from all live servers
    }

    private ReservationServer getServerById(int id)
    {
        for(int i = 0;i<servers.size();i++)
            if(servers.get(i).getId()==id)
                return servers.get(i);

        //todo throw exception here
        return null;
    }

    private CSRequest getTheSmallestRequest()
    {
        int minTs = csRquestQueue.get(0).get_timeStamp();
        CSRequest minRequest = csRquestQueue.get(0);
        for(int i = 1 ;i<csRquestQueue.size();i++)
        {
            if(csRquestQueue.get(i).get_timeStamp() < minTs)
            {
                minTs = csRquestQueue.get(i).get_timeStamp();
                minRequest = csRquestQueue.get(i);
            }
        }

        return minRequest;
    }
}
