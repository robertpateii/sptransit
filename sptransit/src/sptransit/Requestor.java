package sptransit;
import java.io.Serializable;
import java.util.logging.Logger;

public class Requestor extends BaseSocket {
    private TAddress endPoint;
    Logger log;

    public Requestor (Logger log) {
        super(log);
        this.log = log;
    }

    public void connect(String host, int port) {
        endPoint = new TAddress(host, port);
        // clients will need a hidden listener server to get replies
        bind("localhost", 0);
    }
    //TODO : add code to create a default receiving port, when the socket is instantiated as a client
    // idea: client calls socket.connect() before it sends so why not set the port then. this mirrors
    // how the server works, its port is set when it calls socket.bind().
    // also it seems risky and confusing to use our public bind method internally to create a listener
    // socket for the client. let's make some private method to deal with that issue.

    public void send(Serializable message) {
        super.send(message, endPoint);
    }

    public Serializable receive() {
        TPacket packet = super.receivePacket();
        return packet.message;
    }
}
