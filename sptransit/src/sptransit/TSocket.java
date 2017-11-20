package sptransit;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.SynchronousQueue;

public class TSocket {
    private TContext _TContext;
    private String _bindEndPointHost;
    private int _bindEndPointPort;
    private String _connectEndPointHost;
    private int _connectEndPointPort;
    private Runnable _serverRunner;
    private SynchronousQueue<TMessage> _messageQueue;

    public TSocket(TContext tcontext) {
        _TContext = tcontext;
        _messageQueue = new SynchronousQueue<>();
        _TContext._sockets.add(this);

        //TODO : add code to create a default receiving port, when the socket is instantiated as a client
    }

    public void bind(String host, int port) {
        _bindEndPointHost = host;
        _bindEndPointPort = port;

        _serverRunner = new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        //TODO : this should create another thread not to block the server thread
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                        PrintWriter out =
                                new PrintWriter(clientSocket.getOutputStream(), true);
                        TMessage messge = null;
                        try {
                            messge = (TMessage) in.readObject();
                            _messageQueue.put(messge);

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        out.write("ACK");
                        out.flush();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Accept failed : ");
                }
            }
        };

        _serverRunner.run();
    }

    public void connect(String host, int port) {
        _connectEndPointHost = host;
        _connectEndPointPort = port;
    }

    public void send(TMessage message) {
        send(message, new TAddress(_connectEndPointHost, _connectEndPointPort));
    }

    public void send(TMessage message, TAddress addy) {
        String host = addy.address;
        int port = addy.port;

        InetAddress ia;
        try {
            ia = InetAddress.getByName(host);
            Socket s = new Socket(ia, port);

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            //set the source of the message here?
            message.setSourceAddress(InetAddress.getLocalHost().toString(), _bindEndPointPort);

            pout.writeObject(message);
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
        while (_messageQueue.isEmpty()) {
            // waiting
        }
        TMessage oldest = _messageQueue.poll();
        _TContext.setLastSender(new TAddress(oldest.getIpAddress(), oldest.getPort()));
        return oldest;
    }

    /**
     * Assumes a receive() has taken place recently and sends the message to the
     * last sender.
     * @param reply The reply message, same as TMessage but no target address
     */
    public void reply(TReply reply) {
        TMessage replyMessage = new TMessage(reply.getBody());
        send(replyMessage, _TContext.getLastSender());
    }
}