package sptransit;

import java.util.ArrayList;
import java.util.logging.Logger;

public class TContext {
    ArrayList<sptransit.TSocket> _sockets;
    TAddress _lastSender;
    Logger log;

    public TContext() {
        _sockets = new ArrayList<>();
        this.log = Logger.getLogger("sptransit.tcontext");
    }

    public TContext(Logger logger) {
        _sockets = new ArrayList<>();
        this.log = logger;
    }

    public void setLastSender(TAddress addy) {
        _lastSender = addy;
    }

    public TAddress getLastSender() {
        return _lastSender;
    }
}
