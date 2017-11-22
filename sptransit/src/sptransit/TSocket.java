package sptransit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TSocket {
    private TContext _TContext;
    private TAddress _bindEndPointAddress;
    private TAddress _connectEndPointAddress;
    private Thread _serverRunnerThread;
    private ConcurrentLinkedQueue<TPacket> _messageQueue;

    public TSocket(TContext tcontext) {
        _TContext = tcontext;
        _messageQueue = new ConcurrentLinkedQueue<>();
        _TContext.sockets.add(this);

        //TODO : add code to create a default receiving port, when the socket is instantiated as a client
    }

    /**
     * Servers bind to an address to open it and begin queuing messages
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
                        _TContext.log.info("binding to a random port to incoming messages");
                        _bindEndPointAddress.setPort(serverSocket.getLocalPort());
                    }

                    _TContext.log.info("Starting listening for incoming messages on " + port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        _TContext.log.info("Received message, Reading Packet Object");
                        //TODO : this should create another thread not to block the server thread
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                        PrintWriter out =
                                new PrintWriter(clientSocket.getOutputStream(), true);
                        TPacket packet;
                        try {
                            packet = (TPacket) in.readObject();
                            _messageQueue.add(packet);

                        } catch (ClassNotFoundException e) {
                            _TContext.log.severe(e.getMessage());
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

    /**
     * Clients connect to a server in order to send and receive messages
     * @param host
     * @param port
     */
    public void connect(String host, int port) {
        _connectEndPointAddress = new TAddress(host, port);
    }

    public void send(Serializable message) {
        send(message, _connectEndPointAddress);
    }

    private void send(Serializable message, TAddress address) {
        _TContext.log.info("Prepping for send");

        //if the socket is not bound to an explicit ip
        //then bind to random port and start listening
        if (_bindEndPointAddress == null) {
            _TContext.log.info("binding to the next available port");
            bind("localhost", 0);
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

    public Serializable receive() {
        _TContext.log.info("Attempting to receive message");
        while (_messageQueue.isEmpty()) {
            // waiting
            try {
                Thread.sleep(100);
                //_TContext.log.info("wating for 100 milli seconds before trying again");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TPacket oldest = _messageQueue.poll();
        _TContext.lastSender = oldest.address;
        _TContext.log.info("received message");
        return oldest.message;
    }

    /**
     * Assumes a receive() has taken place recently and sends the message to the
     * last sender.
     *
     * @param reply The reply object which must be serializable
     */
    public void reply(Serializable reply) {
        send(reply, _TContext.lastSender);
    }

    /**
     * @return
     */
    public boolean peek() {
        return !_messageQueue.isEmpty();
    }
}