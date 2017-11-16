package Samples.InventoryManagement;

import java.net.*;
import java.io.*;

public class Client {

    public static void main(String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        String mode = "T";

        System.out.println("Welcome to inventory service client");
        System.out.println("please enter one of these commands");
        System.out.println("setmode T|U");
        System.out.println("purchase <user-name> <product-name> <quantity>");
        System.out.println("cancel <order-id>");
        System.out.println("search <user-name>");
        System.out.println("list");
        System.out.println("q to quit");

        try {
            while (true) {
                BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
                String command = stdinp.readLine();
                String commandType = command.split(" ")[0];

                if (commandType.equals("setmode")) {
                    String arg = command.split(" ")[1].toUpperCase();
                    mode = arg;
                    System.out.println("ok, mode is " + arg);
                } else if (commandType.equals("q")) {
                    break;
                } else {
                    send(mode, command, hostAddress, tcpPort, udpPort);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return;
    }

    public static void send(String mode, String command, String hostAddress, int tcpPort, int udpPort) {

        try {
            InetAddress ia = InetAddress.getByName(hostAddress);
            int len = 1024;
            if (mode.equals("T")) {
                try {
                    Socket server = new Socket(ia, tcpPort);
                    BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    DataOutputStream pout = new DataOutputStream(server.getOutputStream());

                    pout.writeBytes(command + '\n');
                    pout.flush();

                    String retValue = din.readLine(); // scanner next time
                    String[] lines = retValue.split("-");
                    for (String line : lines) {
                        System.out.println(line);
                    }

                    server.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
            } else if (mode.equals("U")) {
                try {
                    byte[] buffer = new byte[command.length()];
                    buffer = command.getBytes();
                    DatagramPacket sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
                    DatagramSocket dataSocket = new DatagramSocket();
                    dataSocket.send(sPacket);

                    byte[] rbuffer = new byte[len];
                    DatagramPacket rPacket = new DatagramPacket(rbuffer, rbuffer.length);
                    dataSocket.receive(rPacket);

                    String retString = new String(rPacket.getData(), 0, rPacket.getLength());
                    String[] lines = retString.split("-"); // couldn't get line separator to  work in netbeans console
                    for (String line : lines) {
                        System.out.println(line);
                    }

                } catch (SocketException e) {
                    System.err.println(e);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
