package inventory;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import sptransit.*;

public class Server {

    public static void main(String[] args) throws IOException {
        int tcpPort;
        if (args.length != 2) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <file>: the file of inventory");

            System.exit(-1);
        }
        tcpPort = Integer.parseInt(args[0]);
        String fileName = args[1];
        Path path = Paths.get("inputs\\" + fileName);
        System.out.println("Getting inventory file: " + path.toString());
        List<String> lines;
        lines = Files.readAllLines(path);
        if (lines.size() > 0) {
            lines.forEach((String line) -> {
                System.out.println(line);
            });
        } else {
            System.out.println("Found nothing in inventory file.");
        }

        // parse the inventory file
        InventoryManager.Initialize(lines);

        Replier socket = new Replier();
        socket.bind("localhost", tcpPort);
        while (true) {
            String msg = (String)socket.receive();
            System.out.println("Received " + msg.toString());
            String output = InventoryManager.HandleCommand(msg);
            socket.reply(output);
        }

    }
}
