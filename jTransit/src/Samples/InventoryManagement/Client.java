package Samples.InventoryManagement;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    /* this is straight out of the chapter for UDP. It sends a command and then
    waits for a UDP packet to hit its port.
    next step: add a TCP version that's the default, and switche between them
    when the setmode command comes in.
    */
    public static void main(String[] args) {
        String mode = "T";

        System.out.println("Welcome to inventory service client");
        System.out.println("please enter one of these commands");
        System.out.println("setmode T|U");
        System.out.println("purchase <user-name> <product-name> <quantity>");
        System.out.println("cancel <order-id>");
        System.out.println("search <user-name>");
        System.out.println("list");
        System.out.println("q to quit");

        try
        {
            while(true){
                BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
                String command = stdinp.readLine();
                String commandType = command.split(" ")[0];

                if(commandType.equals("setmode"))
                {
                    String arg = command.split(" ")[1].toUpperCase();
                    mode = arg;
                    System.out.println("ok, mode is " + arg);
                }
                else if (commandType.equals("q"))
                {
                    break;
                }
                else
                {
                    send(mode,command);
                }
            }
        }catch(IOException e)
        {
            System.err.println(e);
        }
        return;
    }

    public static void send(String mode,String command )
    {
        int tcp_port = 3007,udp_port=3008;
        String hostname="localhost";

        try
        {
            InetAddress ia = InetAddress.getByName(hostname);
            int len = 1024;
            if(mode.equals("T"))
            {
                try
                {
                    Socket server = new Socket(ia,tcp_port);
                    BufferedReader din = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    DataOutputStream pout = new DataOutputStream(server.getOutputStream());

                    pout.writeBytes(command+'\n');
                    pout.flush();

                    String retValue = din.readLine(); // scanner next time
                    String[] lines = retValue.split("-");
                    for (String line : lines) {
                        System.out.println(line);
                    }

                    server.close();
                }catch (IOException e)
                {
                    System.err.println(e);
                }
            }
            else if (mode.equals("U"))
            {
                try
                {
                    byte[] buffer = new byte[command.length()];
                    buffer = command.getBytes();
                    DatagramPacket sPacket  = new DatagramPacket(buffer,buffer.length,ia, udp_port);
                    DatagramSocket dataSocket = new DatagramSocket();
                    dataSocket.send(sPacket);

                    byte[] rbuffer = new byte[len];
                    DatagramPacket rPacket = new DatagramPacket(rbuffer,rbuffer.length);
                    dataSocket.receive(rPacket);

                    String retString = new String(rPacket.getData(),0,rPacket.getLength());
                    String[] lines = retString.split("-"); // couldn't get line separator to  work in netbeans console
                    for (String line : lines) {
                        System.out.println(line);
                    }

                } catch (SocketException e) {
                    System.err.println(e);
                }
            }
        } catch (IOException e)
        {
            System.err.println(e);
        }
    }
}