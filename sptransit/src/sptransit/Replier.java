package sptransit;
import java.io.Serializable;
import java.util.logging.Logger;

public class Replier extends BaseSocket {
    private TAddress lastSender;

    public Replier(Logger log) {
        super(log);
    }

    public Replier() {
        super();
    }

    public void bind(String host, int port) {
        super.bind(host, port);
    }

    public Serializable receive() {
        TPacket packet = super.receivePacket();
        lastSender = packet.address;
        return packet.message;
    }

    public void reply(Serializable reply) {
        super.send(reply, lastSender);
    }

}
