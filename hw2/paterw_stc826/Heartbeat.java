
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
                BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
                DataOutputStream pout = new DataOutputStream(server.getOutputStream());
                pout.writeBytes("heartbeat" + '\n');
                pout.flush();
                pout.close();
                String retValue = din.readLine();
                din.close();
                System.out.println("Response: " + retValue);
                server.close();
                serverAddresses.add(address);
            } catch (IOException ex) {
                // do nothing, only add good connections to serverAddresses
                System.out.println("Failed heartbeat from " + address);
            }
        }
    }

    protected void onRecieveHeartbeat(Socket pipe) {
        PrintWriter out
                = null;
        try {
            System.out.println("got heartbeat from " + pipe.getRemoteSocketAddress());
            out = new PrintWriter(pipe.getOutputStream(), true);
            out.write("OK");
            out.flush();
            out.close();
            pipe.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
