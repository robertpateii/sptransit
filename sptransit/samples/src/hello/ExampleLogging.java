package hello;

import java.util.logging.*;

public class ExampleLogging {
    private static Logger log;

    public static void main(String[] args) {
        log = HelloLogger.setup("example");
        log.info("Testing logging!");
    }
}
