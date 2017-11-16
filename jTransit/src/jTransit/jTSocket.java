package jTransit;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class jTSocket {
    private jTContext _jTContext;
    private String _bindEndPoint;
    private String _connectEndPoint;

    public  jTSocket(jTContext jtcontext)
    {
        _jTContext = jtcontext;
    }

    public void bind(String endpoint) {
        _bindEndPoint = endpoint;
    }

    public void connect(String endpoint){
        _connectEndPoint = endpoint;
        //todo : just connect and keep listening to messages here ?
    }

    public void Send(jTMessage message) {
        //open a the tcp
        Pattern pattern = Pattern.compile("");
        Matcher matcher = pattern.matcher(_connectEndPoint);
        matcher.find();

        String host = matcher.group(1);
        int port = Integer.parseInt(matcher.group(2));

        InetAddress ia;
        try {
            ia = InetAddress.getByName(host);

            Socket s = new Socket(ia, port);
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ObjectOutputStream pout = new ObjectOutputStream(s.getOutputStream());

            pout.writeObject(message);
            pout.flush();

            String retValue = din.readLine(); // scanner next time

            //TODO : add code to ensure that the receiver sent an ACK message back

            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public jTMessage Receive(){

        try{
            //todo : validate that the end point address has been initialized
            Pattern pattern = Pattern.compile("");
            Matcher matcher = pattern.matcher(_bindEndPoint);
            matcher.find();

            String host = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));

            System.out.println("TCP server starting");
            ServerSocket listener = new ServerSocket(port);
            Socket s;
            while((s = listener.accept())!= null)
            {
                PrintWriter out =
                        new PrintWriter(s.getOutputStream(), true);
                ObjectInputStream in = new ObjectInputStream (s.getInputStream());

                jTMessage messge = null;
                try {
                    messge = (jTMessage) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                out.write("ACK");
                out.flush();
                s.close();

                return messge;
            }
        } catch(IOException e)
        {
            System.err.print(e);
        }

        return null;
    }
}

