package sptransit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

class BaseSocket {

    private Logger log;
    private TAddress _bindEndPointAddress;
    private TAddress _connectEndPointAddress;
    private Thread _serverRunnerThread;
    private ConcurrentLinkedQueue<TPacket> messageQueue;

    public BaseSocket(Logger log) {
        messageQueue = new ConcurrentLinkedQueue<>();
        this.log = log;
    }

    /**
     * Servers bind to an address to open it and begin queuing messages
     *
     * @param host
     * @param port
     */
    public void bind(String host, int port) {
        _bindEndPointAddress = new TAddress(host, port);

        Runnable serverRunner = new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);

                    //if port is randomly generated update the binding address
                    //TODO : in a network this might not work, since the port has to be allowed thru the fire all,
                    //ip range might be the solution ... keep thinking
                    if (port == 0) {
                        log.info("binding to a random port to incoming messages");
                        _bindEndPointAddress.setPort(serverSocket.getLocalPort());
                    }

                    log.info("Starting listening for incoming messages on " + port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        log.info("Received message, Reading Packet Object");
                        //TODO : this should create another thread not to block the server thread
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                        PrintWriter out =
                                new PrintWriter(clientSocket.getOutputStream(), true);
                        TPacket packet;
                        try {
                            packet = (TPacket) in.readObject();
                            messageQueue.add(packet);

                        } catch (ClassNotFoundException e) {
                            log.severe(e.getMessage());
                        }
                        out.write("ACK");
                        out.flush();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Accept failed : ");
                    e.printStackTrace();
                }
            }
        };
        _serverRunnerThread = new Thread(serverRunner);
        _serverRunnerThread.start();
    }


    protected void send(Serializable message, TAddress address) {
        log.info("Prepping for send");

        if (_bindEndPointAddress == null) {
            throw new RuntimeException("Unexpected sending without an address to recieve a reply");
        }

        try {
            Socket s = new Socket(address.getIPAddress(), address.getPort());

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            TPacket packet = new TPacket(message, _bindEndPointAddress);

            pout.writeObject(packet);
            pout.flush();
            pout.close();

            // TODO: see if this works with no din since we don't do anything with it
            String retValue = din.readLine(); // scanner next time

            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    protected TPacket receivePacket() {
        log.info("Attempting to receive message");
        while (messageQueue.isEmpty()) {
            // waiting
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("received packet");
        return messageQueue.poll();

    }

    /**
     * @return
     */
    public boolean peek() {
        return !messageQueue.isEmpty();
    }
}
