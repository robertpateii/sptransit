package hello;

import java.util.logging.*;
import java.io.*;

public class HelloLogger {
    private static Logger logger = Logger.getLogger("samples.hello");
    private static FileHandler fh;

    /**
     * Only run this once per application. If you need to get the logger afterwards
     * call Logger.getLogger("samples.hello")
     */
    public static Logger setup() {
        try {
            fh = new FileHandler("out/samples-hello.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.addHandler(fh);
        logger.setLevel(Level.INFO);
        logger.info("Logger setup!");
        return logger;
    }
}
