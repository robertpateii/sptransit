package inventory;

import java.io.*;
import java.util.Scanner;

import sptransit.*;

public class Client {
    static Requestor requestor;

    public static void main(String[] args) {
        String hostAddress;
        int tcpPort;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        requestor = new Requestor();
        requestor.connect(hostAddress, tcpPort);

        System.out.println("Welcome to inventory service client");
        System.out.println("please enter one of these commands");
        System.out.println("purchase <user-name> <product-name> <quantity>");
        System.out.println("cancel <order-id>");
        System.out.println("search <user-name>");
        System.out.println("list");
        System.out.println("q to quit");

        while (true) {
            Scanner sc = new Scanner(System.in);
            String commandType = sc.next();

            if (commandType.equals("q")) {
                break;
            } else {
                requestor.send(commandType + sc.nextLine());
                String output = (String) requestor.receive();
                System.out.println(output);
            }
        }
        return;
    }
}
