package sptransit;

import java.io.*;
import java.net.InetAddress;
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
        _TContext._sockets.add(this);

        //TODO : add code to create a default receiving port, when the socket is instantiated as a client
    }

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
                        _bindEndPointAddress.set_port(serverSocket.getLocalPort());
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

    public void connect(String host, int port) {
        _connectEndPointAddress = new TAddress(host, port);
    }

    public void send(TMessage message) {
        send(message, _connectEndPointAddress);
    }

    public void send(TMessage message, TAddress addy) {
        _TContext.log.info("Prepping for send");
        String host = addy.get_ipaddress();
        int port = addy.get_port();


        //if the socket is not bound to an explicit ip
        //then bind to random port and start listening
        if (_bindEndPointAddress == null) {
            _TContext.log.info("binding to the next available port");
            bind("localhost", 0);
        }

        InetAddress ia;
        try {
            ia = InetAddress.getByName(host);
            Socket s = new Socket(ia, port);

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            //set the source of the message here?
            // eh seems weird here, maybe we have two classes one without address if the user
            // doesn't need to put in the address, like i did with TReply. So maybe TPacket
            // has the message and the address and TMessage just has the body. Then TReply
            // would be removed and replaced with TMessage and send would take a TMessage
            // and a TAddress OR a TPacket.
            TPacket packet = new TPacket(message, _bindEndPointAddress);

            pout.writeObject(packet);
            pout.flush();

            String retValue = din.readLine(); // scanner next time

            //TODO : add code to ensure that the receiver sent an ACK message back
            // rob: wait on second thought, do we even need an ack back? It's tcp.
            // the client knows its message was delivered regardless of ack because it's
            // tcp stream. right?

            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public TMessage receive() {
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
        _TContext.setLastSender(new TAddress(oldest.get_address().get_ipaddress(), oldest.get_address().get_port()));
        _TContext.log.info("received message");
        return oldest.get_message();
    }

    /**
     * Assumes a receive() has taken place recently and sends the message to the
     * last sender.
     *
     * @param reply The reply message, same as TMessage but no target address
     */
    public void reply(TReply reply) {
        TMessage replyMessage = new TMessage(reply.getBody());
        send(replyMessage, _TContext.getLastSender());
    }

    /**
     * @return
     */
    public boolean peak() {
        return !_messageQueue.isEmpty();
    }
}