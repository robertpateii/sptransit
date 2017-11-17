package hello;
import sptransit.*;

public class Server {
    public static void main (String[] args) {
        TContext context = new TContext();
        TMessage msg = new TMessage<String>();
        msg.Body = "World";
    }
}
