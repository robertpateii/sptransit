package hello;

import java.util.logging.*;
import java.io.*;

public class HelloLogger {
    private static Logger logger;
    private static FileHandler fh;

    /**
     * Only run this once per main method. If you need to get the logger afterwards
     * call Logger.getLogger("samples.source")
     * @param source the class name that will go into the file name
     * @return a reference to the logger
     */
    public static Logger setup(String source) {
        logger = Logger.getLogger("samples." + source);
        source = source.toLowerCase();
        try {
            fh = new FileHandler("out/samples-" + source + ".log");
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
