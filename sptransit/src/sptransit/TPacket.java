package sptransit;

import java.io.Serializable;

public class TPacket implements java.io.Serializable {
    final Serializable message;
    final TAddress address;

    public TPacket(Serializable message, TAddress address) {
        this.message = message;
        this.address = address;
    }
}
