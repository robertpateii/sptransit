package inventory;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {
        int tcpPort;
        int udpPort;
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(2) <udpPort>: the port number for UDP connection");
            System.out.println("\t(3) <file>: the file of inventory");

            System.exit(-1);
        }
        tcpPort = Integer.parseInt(args[0]);
        udpPort = Integer.parseInt(args[1]);
        String fileName = args[2];
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

        // open both tcp and udp sockets - clients may be either
        TCPServerRunner tcpRunner = new TCPServerRunner(tcpPort);
        Thread t1 = new Thread(tcpRunner, "TCP");
        t1.start();

        UDPServerRunner udpRunner = new UDPServerRunner(udpPort);
        Thread t2 = new Thread(udpRunner, "UDP");
        t2.start();
    }
}
