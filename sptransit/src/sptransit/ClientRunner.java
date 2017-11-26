package sptransit;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class ClientRunner extends Thread {

    Socket clientSocket;
    Logger log;
    ConcurrentLinkedQueue messageQueue;

    public ClientRunner(Socket clientSocket, Logger log, ConcurrentLinkedQueue messageQueue) {
        this.clientSocket = clientSocket;
        this.log = log;
        this.messageQueue = messageQueue;
    }

    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            TPacket packet;
            packet = (TPacket) in.readObject();
            if (packet.message.toString().equals("Hi from p1 -> p3 (causally first for p3)")) {
                log.info("delaying for causal test");
                // delay receiving to simulate slow network for this message
                // in order to test the causal ordering package
                Thread.sleep(2000);
            }
            messageQueue.add(packet);
            in.close();
            clientSocket.close();
        } catch (ClassNotFoundException e) {
            log.severe(e.getMessage());
        } catch (InterruptedException e) {
            log.severe(e.getMessage());
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
    }
}
