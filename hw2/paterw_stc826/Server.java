import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
  public static void main (String[] args) {

    Scanner sc = new Scanner(System.in);
    int myID = sc.nextInt();
    int numServer = sc.nextInt();
    int numSeat = sc.nextInt();

    String currentServerAddress = null;
    ArrayList<String> serverAddresses = new ArrayList<String>();
    for (int i = 0; i < numServer; i++) {
        String temp = sc.next();
        serverAddresses.add(temp);
        if(i==myID-1)
          currentServerAddress = temp;
    }

    ReservationManager.Initialize(numSeat);
    ServerRunner runner = new ServerRunner(Integer.parseInt(currentServerAddress.split(":")[1]));
    Thread t = new Thread(runner,"Reservation Server");
    t.start();

    // TODO: handle request from clients
  }
}