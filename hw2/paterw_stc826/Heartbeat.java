
import java.util.*;
import java.net.*;
import java.io.*;

public class Heartbeat {

    private ArrayList<InetSocketAddress> serverAddresses;
    private InetSocketAddress myAddress;
    private Server parentServer;

    Heartbeat(ArrayList<InetSocketAddress> inputServers, Server s) {
        this.serverAddresses = s.serverAddresses; // not making a new one, main ref is on server
        this.myAddress = s.myAddress;
        parentServer = s;
        startHeartbeat(inputServers);
    }

    private void startHeartbeat(ArrayList<InetSocketAddress> inputServers) {
        // Populate serverAddressses with server that work
        for (InetSocketAddress address : inputServers) {
            if (address.equals(myAddress)) {
                continue;
            }
            try {
                Socket server = new Socket(address.getAddress(), address.getPort());
                server.setSoTimeout(100);
                System.out.println("Sending heartbeat to " + address);
                DataOutputStream pout = new DataOutputStream(server.getOutputStream());
                pout.writeBytes("heartbeat" + '\n');
                pout.flush();
                pout.close();
                serverAddresses.add(address);
                server.close();
                System.out.println("Successful heartbeat from " + address);
            } catch (IOException ex) {
                // do nothing, only add good connections to serverAddresses
                System.out.println("Failed heartbeat from " + address);
                System.out.println(ex);
            }
        }
    }

    protected void onRecieveHeartbeat(Socket pipe) {
        System.out.println("got heartbeat from " + pipe.getRemoteSocketAddress());
    }
}
