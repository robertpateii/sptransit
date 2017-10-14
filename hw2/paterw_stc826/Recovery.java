
import java.util.*;

public class Recovery {

    final boolean wasSuccessful;
    ArrayList<String> seatList;
    Queue<CSRequest> pendingQueue; // timestamp, socket, and command

    public Recovery(Server server) {
        // assumes heartbeat is keeping serverSockets up to date
        int serversUp = server.serverAddresses.size();
        if (serversUp == 0) {
            wasSuccessful = false;
        } else {
            recoverState(); // blocks entire server thread
            wasSuccessful = true;
        }

        // if no other servers up, use empty seat array
    }

    private void sendConnect() {
        // used when coming up from crash to get into others' server list
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // servers send connect when coming back from crash
    void OnReceiveConnect() {
        // add to socket list, assume single threaded server only one command
        // hanlded at a time, no one will be accesssing server socket list
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // this is what servers send back after receiving your connect message
    void OnReceiveRecoveryState() {
        // block until got states from all live servers
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void recoverState() {
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

}
