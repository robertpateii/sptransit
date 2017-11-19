package hello;

import sptransit.*;

public class Client {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup();
        log.info("Starting");

        TContext context = new TContext();
        TMessage msg = new TMessage<String>("Hello");
        TSocket server = new TSocket(context);
        server.bind("localhost", 8000);
        server.send(msg);
        /* context will receive any messages from this server into a queue,
            because of the implication */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /* server.receive() will block if there's no messages yet */
        TMessage<String> reply = server.receive();
        log.info(reply.getBody());
    }
}
