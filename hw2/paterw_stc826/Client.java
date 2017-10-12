
import java.util.*;
import java.net.*;
import java.io.*;

public class Client {

    static Socket currentServer;
    static ArrayList<InetSocketAddress> servers;

    public static void main(String[] args) throws IOException {
        System.out.println("Scanning client input files.");
        Scanner sc = new Scanner(System.in);
        int numServer = sc.nextInt();
        System.out.println("numServer: " + numServer);
        servers = new ArrayList<>();

        for (int i = 0; i < numServer; i++) {
            String temp = sc.next();
            System.out.println("server: " + temp);
            int spacerIndex = temp.indexOf(":");
            String host = temp.substring(0, spacerIndex);
            int port = Integer.parseInt(temp.substring(spacerIndex + 1));
            servers.add(new InetSocketAddress(host, port));
        }
        String leftoverLineBreak = sc.nextLine();

        if (connectToServer() == false) {
            System.out.println("ERROR: Could not connect to any servers");
            return;
        }

        while (sc.hasNextLine()) {
            String cmd = sc.nextLine();
            System.out.println("cmd: " + cmd);
            Socket server = new Socket(servers.get(0).getAddress(), servers.get(0).getPort());
            System.out.println("server to connect to: " + servers.get(0).getHostName() + ":" + servers.get(0).getPort());
            BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
            DataOutputStream pout = new DataOutputStream(server.getOutputStream());

            pout.writeBytes(cmd + '\n');
            pout.flush();

            String retValue = din.readLine(); // scanner next time
            System.out.println("Response: " + retValue);
            server.close();
        }
    }

    private static boolean connectToServer() {
        return true;
        // for each server in servers
        // open socket new Socket(address.getAddress, address.getPort
        // if success, exit loop
        // if fail, remove server from list (per garg)
        // close on error
        // return false if all servers fail
        // don't forget to close at end of main too
    }

}
