package hello;

import sptransit.*;

public class Server {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup();
        log.info("Starting");

        TContext context = new TContext();
        TReply reply = new TReply<String>("World");
        TSocket socket = new TSocket(context);
        socket.bind("localhost",8000);
        while (true) {
            TMessage<String> msg = socket.receive();
            log.info("Received: " + msg.getBody());
            socket.reply(reply);
            log.info("Sent: " + reply.getBody());
        }
    }
}
