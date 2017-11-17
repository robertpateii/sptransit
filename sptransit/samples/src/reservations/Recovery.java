package reservations;

import java.util.*;
import java.net.*;
import java.io.*;

public class Recovery {

    final boolean thisIsOnlyServer;
    ArrayList<String> seatList;
    Queue<CSRequest> pendingQueue; // timestamp, and command
    Server parent;

    public Recovery(Server server) {
        // assumes heartbeat is keeping serverSockets up to date
        this.parent = server;
        int serversUp = server.serverAddresses.size();
        if (serversUp == 0) {
            // if no other servers up, server uses an empty seat array
            thisIsOnlyServer = true;
        } else {
            thisIsOnlyServer = false;
            recoverState(); // blocks entire server thread
        }
    }

    // servers send connect when coming back from crash
    // Need to send them the recovery state
    // add them to our server list now or later?
    void OnReceiveConnect(String msg, Socket newServer) {
        // parse the server info and add to our list
        Scanner sc = new Scanner(msg);
        String command = sc.next();
        if (!command.equals("connect")) System.err.println("Expected connect got " + command);
        String hostName = sc.next();
        String portString = sc.next();
        System.out.println("Connecting server sent host " + hostName + " and " + portString);
        InetSocketAddress newServerAddress = new InetSocketAddress(hostName, Integer.parseInt(portString));
        parent.serverAddresses.add(newServerAddress);
        System.out.println("Added new server as " + newServerAddress.getHostString() + " port " + newServerAddress.getPort());

        seatList = parent.resMgr.seats;
        pendingQueue = new LinkedList<>(parent.mutex.q);
        
        if (seatList == null || pendingQueue == null) {
            throw new RuntimeException("seatList and/or pendingQueue are null, but need to send to new server!");
        } else {
            System.out.println("Sending over seats and queue. Seat examples, 0: " + seatList.get(0) + " and 7: " + seatList.get(7));
        }
        // send 'em the seatList and pendingQueue
        try {
            DataOutputStream pout = new DataOutputStream(newServer.getOutputStream());
            ObjectOutputStream oos= new ObjectOutputStream(pout);
            oos.writeObject(seatList);
            oos.writeObject(pendingQueue);
            System.out.println("Wrote out seatList and pendingQueue to new server.");
            oos.flush();
            oos.close();
            pout.flush();
            pout.close();
            System.out.println("done receiving connect");
        } catch (IOException ex) {
            System.err.println("Receive Connect failed " + ex);
        }

    }

    private boolean recoverState() {
        // send my info to the other servers
        // get back their pendingQueue and seatList
        // replace mine with theirs
        // keep replacing with the latest pendingQueue and seatList as we loop through
        // assumption: the last queue/table is in sync with the rest
        // when done open to client connections
        // when do we get added to the other servers official lists: when they receive connect
        LinkedList<Integer> deadServers = new LinkedList<>();
        for (int i = 0; i < parent.serverAddresses.size(); i++) {
            InetSocketAddress targetAddy = parent.serverAddresses.get(i);
            String myAddy = parent.myAddress.getAddress().toString();
            Integer myPort = parent.myAddress.getPort();
            String myPortStr = myPort.toString();
            try {
                Socket s = new Socket(targetAddy.getAddress(), targetAddy.getPort());
                s.setSoTimeout(100);
                System.out.println("Sending connect to recovery to " + targetAddy.toString());
                DataOutputStream pout = new DataOutputStream(s.getOutputStream());
                pout.writeBytes("connect " + myAddy + " " + myPortStr + "\n");
                // deserialize seatList and pendingQueue
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                seatList  = (ArrayList<String>) ois.readObject();
                pendingQueue  = (Queue<CSRequest>) ois.readObject();
                ois.close();
                pout.close();
                // done?
                s.close();
            } catch (IOException ex) {
                System.out.println("Failed recovery connection to: " + targetAddy.toString());
                deadServers.add(i);
            } catch (ClassNotFoundException exC) {
                System.err.println(exC);
            }
        }
        for (Integer index : deadServers) {
            // handling heartbeat here too, like is server messageAllServers
            parent.serverAddresses.remove(index.intValue());
        }

        // if recovery fails we should throw an exception actually
        if (seatList == null || pendingQueue == null) {
            throw new RuntimeException("Recovery completed but seatList and/or pendingQueue are null");
        } else {
            System.out.println("Recovered seat list examples, 0: " + seatList.get(0) + " and 7: " + seatList.get(7));
        }
        return true;
    }

}
