package hello;

import sptransit.*;

import java.io.Serializable;

public class Client {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup("client");

        Requestor server = new Requestor(log);
        server.connect("localhost", 8585);
        server.send("Hello"); // send takes only serializable objects
        /* context will receive any messages from this server into a queue,
            because of the implication */
        try {
            // pretend we're doing unrelated work that doesn't need the reply
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /* server.receive() will block if there's no messages yet */
        Serializable reply = server.receive();
        log.info("Received: " + reply.toString());

        //TODO : add sample implementation for socket peek
    }
}
