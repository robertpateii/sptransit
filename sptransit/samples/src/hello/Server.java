package hello;

import sptransit.*;

import java.io.Serializable;

public class Server {
    private static java.util.logging.Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup("server");

        Replier socket = new Replier(log);
        socket.bind("localhost", 8585);
        while (true) {
            Serializable msg = socket.receive();
            log.info("Received: " + msg.toString());
            socket.reply("World");
        }
    }
}