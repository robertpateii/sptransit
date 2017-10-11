
import java.util.*;
import java.net.*;
import java.io.*;

public class Heartbeat {

    private HashSet<Socket> serverSockets;
    private InetSocketAddress myAddress;

    Heartbeat(ArrayList<InetSocketAddress> inputServers, Server s) {
        startHeartbeat(inputServers);
        this.serverSockets = s.serverSockets; // not making a new one, main ref is on server
        this.myAddress = s.myAddress;
    }

    private void startHeartbeat(ArrayList<InetSocketAddress> inputServers) {
        // Create sockets for all the server addresses that work
        for (InetSocketAddress address : inputServers) {
            if (address.equals(myAddress)) {
                continue;
            }
            Socket s = new Socket();
            try {
                s.connect(address, 100);
            } catch (IOException ex) {
                // do nothing, only add good connections to serverSockets
            }
            if (!serverSockets.add(s)) { // this adds it to serverSockets
                System.err.println("ERROR: Socktet already in the set.");
            }
        }
        maintainHeartbeat();
    }

    private void maintainHeartbeat() {
        for (Socket s : serverSockets) {
            // send heartbeat message
        }

        /*	1. Start heartbeat to all servers in list
            2. Remove dead servers
            assume 1 and 2 is continuous!
            prevents us from sending other messages to dead servers
         */
    }

    protected void onRecieveHeartbeat(Socket pipe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
