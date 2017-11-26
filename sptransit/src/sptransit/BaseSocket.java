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

    public BaseSocket() {
        messageQueue = new ConcurrentLinkedQueue<>();
        this.log = Logger.getAnonymousLogger();
    }

    /**
     * Servers bind to an address to open it and begin queuing messages
     *
     * @param host publicly available ip/hostname to receive replies
     * @param port can be 0 to use any free random port
     */
    protected void bind(String host, int port) {
        // TODO: bind should only be called once per socket otherwise bindEndPointAddress gets overwritten
        // bundle it into the constructors for the sockets that need it (all of them except requestor?)
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
                        Thread t = new ClientRunner(clientSocket, log, messageQueue);
                        t.start();
                    }
                } catch (IOException e) {
                    log.severe(e.getMessage());
                }
            }
        };
        _serverRunnerThread = new Thread(serverRunner);
        _serverRunnerThread.start();
    }

    protected void sendOneWay(Serializable message, TAddress address) {
        log.info(String.format("Sending to %1$s:%2$s", address.getIPAddress(), address.getPort()));
        try {
            Socket s = new Socket(address.getIPAddress(), address.getPort());

            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            TPacket packet = new TPacket(message, null);

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

    protected void send(Serializable message, TAddress address) {

        log.info(String.format("Sending to %1$s:%2$s", address.getIPAddress(), address.getPort()));

        if (_bindEndPointAddress == null) {
            throw new RuntimeException("Unexpected sending without an address to recieve a reply");
        }
        log.info(String.format("Endpoint is %1$s:%2$s", _bindEndPointAddress.getIPAddress(), _bindEndPointAddress.getPort()));

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
