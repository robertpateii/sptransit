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
        String commandExamples = "purchase <user-name> <product-name> <quantity>" + System.lineSeparator() +
        "cancel <order-id>" + System.lineSeparator() +
        "search <user-name>" + System.lineSeparator() +
        "list" + System.lineSeparator() +
        "help for this list again" + System.lineSeparator() +
        "q to quit";
        System.out.println(commandExamples);

        while (true) {
            Scanner sc = new Scanner(System.in);
            String commandType = sc.next();

            if (commandType.equals("q")) {
                break;
            } else if (commandType.startsWith("h")) {
               System.out.println(commandExamples);
            } else {
                requestor.send(commandType + sc.nextLine());
                String output = (String) requestor.receive();
                System.out.println(output);
            }
        }
        return;
    }
}
