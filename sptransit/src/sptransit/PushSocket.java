package sptransit;

import java.io.Serializable;
import java.util.logging.Logger;

public class PushSocket extends BaseSocket {

    public PushSocket (Logger log) {
        super(log);
    }

    public PushSocket() {
       super();
    }

    public void send(Serializable message, String host, int port) {
        super.sendOneWay(message, new TAddress(host, port));
    }

    public void bind(String host, int port) {
        super.bind(host, port);
    }

    public Serializable receive() {
        return super.receivePacket().message;
    }
}
