
import java.util.ArrayList;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Server {

    static ArrayList<String> seats; // aka seat table
    static ArrayList<InetSocketAddress> servers; // used by recovery/heartbeat?
    static ArrayList<Socket> serverSockets; // kept up to date by heartbeat, used by lamport algorithm
    static InetSocketAddress myAddress; // ??do we need this or an index?
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

        Scanner sc = new Scanner(System.in);
        int myID = sc.nextInt();
        int numServer = sc.nextInt();
        int numSeat = sc.nextInt();
        servers = new ArrayList<>();
        /* index is the seat number, string is the reserved name,
            null is not reserved */
        seats = new ArrayList<>(numSeat);

        for (int i = 0; i < numServer; i++) {
            String temp = sc.next();
            int spacerIndex = temp.indexOf(":");
            String host = temp.substring(0, spacerIndex);
            int port = Integer.parseInt(temp.substring(spacerIndex + 1));
            servers.add(new InetSocketAddress(host, port));
        }
        myAddress = servers.get(myID - 1); // Server ID is 1-indexed
        
        Server thisServer = new Server();
        thisServer.go();
    }

    private void go() {
        StartHeartbeat(); // heartbeat prunes dead servers
        RecoverState(); // if no other servers up, use empty seat array
        OpenConnection(myAddress.getPort());
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

            switch(command)
            {
                //client commands
                case "reserve":
                case "bookSeat":
                case "search":
                case "delete":
                    requestCriticalSection(message,pipe);
                    break;
                //server commands
                case "requestCS":
                    onReceiveRequest();
                    break;
                case "ack":
                    onReceiveAck();
                    break;
                case "release":
                    onReceiveRelease();
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

    private void handleClient(Socket pipe) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(pipe.getInputStream()));
            String command = in.readLine();
            // if command is a search then return and close
            requestCriticalSection(message, pipe);
            // put command into queue with the pipe
        } catch (IOException e) {
            System.err.print(e);
        }

    }

    // for changes from other servers
    private String handleCommand(String command) {
        command = command.trim().toLowerCase();
        String[] options = command.split(" ");
        String commandType = options[0];
        String response;
        switch (commandType) {
            case "reserve":
                response = reserve(options);
            case "bookSeat":
                response = bookSeat(options);
            case "search":
                response = search(options);
            case "delete":
                response = delete(options);
            default:
                response = "Invalid command type: " + commandType;
        }
        return response;
    }
    
    // for our clients
    private void handleCommand(String command, Socket pipe) throws IOException {
        String response = handleCommand(command);
        // pipe stuff
        PrintWriter out
                = new PrintWriter(pipe.getOutputStream(), true);
        out.write(response);
        out.flush();
        pipe.close();
    }


    private void requestCriticalSection(String message, Socket pipe) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onReceiveRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void onReceiveAck() {
        // numacks += 1;
        // if numacks = N - 1 and my request is smallest in q {
            // enterCriticalSection
            // }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    private void onReceiveRelease() {
        // check if we're the top
        // if so enterCriticalSection
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    private void enterCriticalSection() {
        release();
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void release() {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    private String reserve(String[] options) {
        String name = options[1];

        if (seats.contains(name)) {
            return "Seat already booked against the name provided";
        }

        int firstAvailableIndex = -1;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i) != null) {
                firstAvailableIndex = i;
                break;
            }
        }

        if (firstAvailableIndex > -1) {
            seats.set(firstAvailableIndex, name);
            return "Seat assigned to you is " + firstAvailableIndex + 1;
        } else {
            return "Sold out - No seat available";
        }
    }

    private String bookSeat(String[] options) {
        String name = options[1];
        int seatNumber = Integer.parseInt(options[2]);
        int seatIndex = seatNumber - 1;

        if (seats.contains(name)) {
            return "Seat already booked against the name provided";
        }

        if (seats.get(seatIndex) == null) {
            seats.set(seatIndex, name);
            return "Seat assigned to you is " + seatNumber;
        } else {
            return seatNumber + " is not available";
        }
    }

    private String search(String[] options) {
        String name = options[1];

        if (seats.contains(name)) {
            return Integer.toString(seats.indexOf(name) + 1);
        }
        return "No reservation found for " + name;
    }

    private String delete(String[] options) {
        String name = options[1];

        if (seats.contains(name)) {
            int seatIndex = seats.indexOf(name);
            seats.set(seatIndex, null);
            return Integer.toString(seatIndex + 1);
        }
        return "No reservation found for " + name;
    }
}
