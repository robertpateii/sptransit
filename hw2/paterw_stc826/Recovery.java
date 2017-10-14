
import java.util.*;
import java.net.*;
import java.io.*;

public class Recovery {

    final boolean wasSuccessful;
    ArrayList<String> seatList;
    Queue<CSRequest> pendingQueue; // timestamp, socket, and command
    Server parent;

    public Recovery(Server server) {
        // assumes heartbeat is keeping serverSockets up to date
        this.parent = server;
        int serversUp = server.serverAddresses.size();
        if (serversUp == 0) {
            wasSuccessful = false;
        } else {
            wasSuccessful = recoverState(); // blocks entire server thread
        }

        // if no other servers up, use empty seat array
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
        InetSocketAddress newServerAddress = new InetSocketAddress(hostName, Integer.parseInt(portString));
        parent.serverAddresses.add(newServerAddress);
        
        // send 'em the seatList and pendingQueue
        
        try {
            DataOutputStream pout = new DataOutputStream(newServer.getOutputStream());
            ObjectOutputStream oos= new ObjectOutputStream(pout);
            oos.writeObject(seatList);
            oos.flush();
            oos.close();
            pout.flush();
            pout.close();

            DataOutputStream pout2 = new DataOutputStream(newServer.getOutputStream());
            ObjectOutputStream oos2= new ObjectOutputStream(pout);
            oos2.writeObject(pendingQueue);
            oos2.flush();
            oos2.close();
            pout2.flush();
            pout2.close();


            // SERIALIZE seatList and Pnedingqueue here:pout.writeBytes("\n")

            // probably don't close it let them close it
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
                pout.flush();
                pout.close();
                // deserialize seatList and pendingQueue
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                seatList  = (ArrayList<String>) ois.readObject();
                ois.close();
                ObjectInputStream ois2 = new ObjectInputStream(s.getInputStream());
                pendingQueue  = (Queue<CSRequest>) ois2.readObject();
                ois2.close();
                // done?
                s.close();
            } catch (IOException ex) {
                System.out.println("Failed server: " + targetAddy.toString());
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
        return true;
    }

}
