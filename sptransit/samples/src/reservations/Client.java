package reservations;

import java.util.*;
import java.net.*;
import java.io.*;

public class Client {

    static Socket currentServer;
    static ArrayList<InetSocketAddress> servers;

    public static void main(String[] args) throws IOException {
        int numServer;
        System.out.println("Scanning client input files.");
        Scanner sc = new Scanner(System.in);
        servers = new ArrayList<>();
        if (args.length > 0) {
            System.out.println("Main: Parsing args");
            numServer = Integer.parseInt(args[0]);
            for (int i = 0; i < numServer; i++) {
                String temp = args[i+1];
                System.out.println("read server: " + temp);
                int spacerIndex = temp.indexOf(":");
                String host = temp.substring(0, spacerIndex);
                int port = Integer.parseInt(temp.substring(spacerIndex + 1));
                System.out.println("adding server " + host + " and " + port);
                servers.add(new InetSocketAddress(host, port));
                System.out.println("Added server " + servers.get(i).getHostString() + " and port " + servers.get(i).getPort());
            }
        }
        else {
            System.out.println("Main: no args scanning for servers");
            numServer = sc.nextInt();
            System.out.println("numServer: " + numServer);
            for (int i = 0; i < numServer; i++) {
                String temp = sc.next();
                System.out.println("read server: " + temp);
                int spacerIndex = temp.indexOf(":");
                String host = temp.substring(0, spacerIndex);
                int port = Integer.parseInt(temp.substring(spacerIndex + 1));
                System.out.println("adding server " + host + " and " + port);
                servers.add(new InetSocketAddress(host, port));
                System.out.println("Added server " + servers.get(i).getHostString() + " and port " + servers.get(i).getPort());
            }
            String leftoverLineBreak = sc.nextLine();
        }

        int connectedServerIndex = 0;
        while (sc.hasNextLine()) {
            String cmd = sc.nextLine();
            System.out.println("cmd: " + cmd);

            boolean failed = true;
            while(failed && connectedServerIndex < servers.size()) {
                InetAddress serverAddy = servers.get(connectedServerIndex).getAddress();
                int serverPort = servers.get(connectedServerIndex).getPort();
                String addyString = serverAddy.getHostAddress() + " and port " + serverPort;
                if (serverAddy == null) {
                    throw new RuntimeException("server addy was null!");
                }
                try {
                    Socket server = new Socket(serverAddy, serverPort);
                    // let's not timeout... for now...  server.setSoTimeout(100);
                    System.out.println("server to connect to: " + addyString);
                    System.out.println("My address: " + server.getLocalAddress() + " port " + server.getLocalPort());
                    BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    DataOutputStream pout = new DataOutputStream(server.getOutputStream());

                    pout.writeBytes(cmd + '\n');
                    pout.flush();

                    System.out.println("Waiting for server to reply.");
                    String retValue = din.readLine(); // scanner next time
                    System.out.println("Response: " + retValue);
                    server.close();
                    failed = false;
                }
                catch(IOException ex)
                {
                    System.out.println(ex);
                    System.out.println("failed to connect to "+ addyString);
                    System.out.println("attempting the next server");
                    connectedServerIndex++;
                }
            }
            if(connectedServerIndex == servers.size())
            {
                connectedServerIndex = 0;
                System.out.println("All my servers were dead, trying more.");
            }
        }
    }
}
