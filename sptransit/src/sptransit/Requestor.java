package sptransit;

import java.io.Serializable;
import java.util.logging.Logger;

public class Requestor extends BaseSocket {
    private TAddress connectPoint;

    public Requestor (Logger log) {
        super(log);
    }

    public Requestor() {
        super();
    }

    public void connect(String host, int port) {
        connectPoint = new TAddress(host, port);
        // clients will need a hidden listener server to get replies
        bind("localhost", 0);
    }

    public void send(Serializable message) {
        super.send(message, connectPoint);
    }

    public Serializable receive() {
        TPacket packet = super.receivePacket();
        return packet.message;
    }
}
