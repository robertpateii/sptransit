package sptransit;

import java.util.ArrayList;

public class TContext {
    ArrayList<sptransit.TSocket> _sockets;
    TAddress _lastSender;

    public TContext() {
        _sockets = new ArrayList<>();
    }

    public void setLastSender(TAddress addy) {
        _lastSender = addy;
    }

    public TAddress getLastSender() {
        return _lastSender;
    }
}
