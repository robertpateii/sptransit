package hello;

import sptransit.*;

import java.io.Serializable;

public class Server {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup("server");
        log.info("Starting");

        TContext context = new TContext(log);
        String reply = "World";
        TSocket socket = new TSocket(context);
        log.info("Attempting to bind");
        socket.bind("localhost", 8585);
        while (true) {
            log.info("Waiting for a message");
            Serializable msg = socket.receive();
            log.info("Received: " + msg.toString());
            socket.reply(reply);
            log.info("Sent: " + reply);
        }
    }
}