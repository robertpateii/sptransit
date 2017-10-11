
package reservation;


import java.util.ArrayList;
import java.util.Scanner;
import java.net.*;
import java.net.InetSocketAddress;

public class Client {
  static Socket currentServer;
  static ArrayList<InetSocketAddress> servers;
  public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    servers = new ArrayList<>();

    for (int i = 0; i < numServer; i++) {
        String temp = sc.next();
        int spacerIndex = temp.indexOf(":");
        String host = temp.substring(0, spacerIndex);
        int port = Integer.parseInt(temp.substring(spacerIndex + 1));
        servers.add(new InetSocketAddress(host, port));
    }

    if (connectToServer() == false) {
        System.out.println("ERROR: Could not connect to any servers");
        return;
    }

    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server for each case
        switch (tokens[0]) {
            case "reserve":
                break;
            case "bookSeat":
                break;
            case "search":
                break;
            case "delete":
                break;
            default:
                System.out.println("ERROR: No such command");
                break;
        }
    }
  }

  private static boolean connectToServer() {
      return false;
      // for each server in servers
        // open socket new Socket(address.getAddress, address.getPort
        // if success, exit loop
        // if fail, remove server from list (per garg)
        // close on error
        // return false if all servers fail
        // don't forget to close at end of main too
  }

}
