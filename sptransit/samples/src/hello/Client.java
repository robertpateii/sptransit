package hello;
import sptransit.*;

public class Client {
    public static void main (String[] args) {
      TContext context = new TContext();
      TMessage msg = new TMessage<String>("Hello");
      TSocket server = new TSocket(context);
      server.bind("localhost",8000);
      server.send(msg);

    }
}
