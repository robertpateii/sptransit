package sptransit;

import java.util.ArrayList;
import java.util.logging.Logger;

public class TContext {
    ArrayList<sptransit.TSocket> sockets;
    TAddress lastSender;
    Logger log;

    public TContext() {
        sockets = new ArrayList<>();
        log = Logger.getLogger("sptransit.tcontext");
    }

    public TContext(Logger logger) {
        sockets = new ArrayList<>();
        log = logger;
    }
}
