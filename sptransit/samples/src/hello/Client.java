package hello;

import sptransit.*;

import java.io.Serializable;

public class Client {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup("client");
        log.info("Starting");

        TContext context = new TContext(log);
        String msg = "Hello";
        TSocket server = new TSocket(context);
        server.connect("localhost", 8585);
        log.info("Connected, attempting to send message");
        server.send(msg); // send takes only serializable objects
        log.info("Sent: " + msg);
        /* context will receive any messages from this server into a queue,
            because of the implication */
        try {
            // pretend we're doing unrelated work that doesn't need the reply
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Done sleeping, waiting on reply");
        /* server.receive() will block if there's no messages yet */
        Serializable reply = server.receive();
        log.info("Received: " + reply.toString());

        //TODO : add sample implementation for socket peek
    }
}
