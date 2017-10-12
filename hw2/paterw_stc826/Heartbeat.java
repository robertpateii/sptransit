
import java.util.*;
import java.net.*;
import java.io.*;

public class Heartbeat {

    private HashSet<Socket> serverSockets;
    private InetSocketAddress myAddress;
    private Server parentServer;

    Heartbeat(ArrayList<InetSocketAddress> inputServers, Server s) {
        this.serverSockets = s.serverSockets; // not making a new one, main ref is on server
        this.myAddress = s.myAddress;
        parentServer = s;
        startHeartbeat(inputServers);
    }

    private void startHeartbeat(ArrayList<InetSocketAddress> inputServers) {
        // Create sockets for all the server addresses that work
        for (InetSocketAddress address : inputServers) {
            if (address.equals(myAddress)) {
                continue;
            }
            try {
                Socket server = new Socket(address.getAddress(), address.getPort());
                server.setSoTimeout(100);
                System.out.println("Sending heartbeat to " + address);
                BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
                DataOutputStream pout = new DataOutputStream(server.getOutputStream());
                pout.writeBytes("heartbeat" + '\n');
                pout.flush();
                String retValue = din.readLine(); // scanner next time
                System.out.println("Response: " + retValue);
                server.close();
                if (!serverSockets.add(server)) { // this adds it to serverSockets
                    System.err.println("ERROR: Socket already in the set.");
                }
            } catch (IOException ex) {
                // do nothing, only add good connections to serverSockets
                System.out.println("Failed heartbeat from " + address);
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
        PrintWriter out
                = null;
        try {
            System.out.println("got heartbeat from " + pipe.getRemoteSocketAddress());
            out = new PrintWriter(pipe.getOutputStream(), true);
            out.write("OK");
            out.flush();
            pipe.close();
        } catch (IOException ex) {
            System.err.println(ex);
        } finally {
            out.close();
        }
    }

}
