package inventory;
import java.net.*;
import java.io.*;
import java.util.*;

public class TCPServerThread extends Thread {

    Socket theClient;

    public TCPServerThread(Socket s) {
        theClient = s;
    }

    public void run() {
        try {
            PrintWriter out
                    = new PrintWriter(theClient.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(theClient.getInputStream()));

            String command = in.readLine();
            System.out.println("TCP - " + command);
            String response = InventoryManager.HandleCommand(command);

            out.write(response);
            out.flush();
            theClient.close();
        } catch (IOException e) {
            System.err.print(e);
        }

    }

}
