package hello;

import sptransit.*;

public class Server {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup("server");
        log.info("Starting");

        TContext context = new TContext(log);
        TReply reply = new TReply<String>("World");
        TSocket socket = new TSocket(context);
        log.info("Attempting to bind");
        socket.bind("localhost", 8585);
        while (true) {
            log.info("Waiting for a message");
            TMessage<String> msg = socket.receive();
            log.info("Received: " + msg.getBody());
            socket.reply(reply);
            log.info("Sent: " + reply.getBody());
        }
    }
}
