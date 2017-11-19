package inventory;

import java.net.*;
import java.io.*;
import java.util.*;

public class UDPServerThread extends Thread {

    DatagramPacket clientPacket;

    public UDPServerThread(DatagramPacket p) {
        clientPacket = p;
    }

    public void run() {
        try {
            byte[] buffer = clientPacket.getData();
            String command = new String(buffer).trim(); // trim otherwise full length of datagram 1024 characters
            System.out.println("UDP - " + command);
            String response = InventoryManager.HandleCommand(command);
            DatagramPacket returnPacket = new DatagramPacket(
                    response.getBytes(),
                    response.length(),
                    clientPacket.getAddress(),
                    clientPacket.getPort());
            DatagramSocket dataSocket = new DatagramSocket();
            dataSocket.send(returnPacket);
        } catch (IOException e) {
            System.err.print(e);
        }

    }

}
