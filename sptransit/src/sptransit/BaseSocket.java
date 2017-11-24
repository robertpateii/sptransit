package sptransit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

class BaseSocket {

    protected Logger log;
    protected TAddress _bindEndPointAddress;
    protected TAddress _connectEndPointAddress;
    protected Thread _serverRunnerThread;
    protected ConcurrentLinkedQueue<TPacket> messageQueue;

    public BaseSocket(Logger log) {
        messageQueue = new ConcurrentLinkedQueue<>();
        this.log = log;
    }

    /**
     * Servers bind to an address to open it and begin queuing messages
     *
     * @param host publicly available ip/hostname to receive replies
     * @param port can be 0 to use any free random port
     */
    protected void bind(String host, int port) {
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
                        int localPort = serverSocket.getLocalPort();
                        _bindEndPointAddress.setPort(localPort);
                        log.info("Listening on random client port " + localPort);
                    } else {
                        log.info("Listening on port " + port);
                    }


                    Socket clientSocket;
                    while ((clientSocket = serverSocket.accept()) != null) {
                        //TODO : this should create another thread not to block the server thread
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                        TPacket packet;
                        try {
                            packet = (TPacket) in.readObject();
                            messageQueue.add(packet);

                        } catch (ClassNotFoundException e) {
                            log.severe(e.getMessage());
                        }
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    log.severe(e.getMessage());
                }
            }
        };
        _serverRunnerThread = new Thread(serverRunner);
        _serverRunnerThread.start();
    }


    protected void send(Serializable message, TAddress address) {

        if (_bindEndPointAddress == null) {
            throw new RuntimeException("Unexpected sending without an address to recieve a reply");
        }

        try {
            Socket s = new Socket(address.getIPAddress(), address.getPort());

            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            TPacket packet = new TPacket(message, _bindEndPointAddress);

            pout.writeObject(packet);
            pout.flush();
            pout.close();

            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    protected TPacket receivePacket() {
        TPacket packet;
        while ((packet = messageQueue.poll()) == null) {
            // keep looping, poll is atomic, non-blocking, and threadsafe
        }
        return packet;
    }

    protected boolean peek() {
        return !messageQueue.isEmpty();
    }
}
