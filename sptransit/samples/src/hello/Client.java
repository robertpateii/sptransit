package hello;
import sptransit.*;

public class Client {
    public static void main (String[] args) {
      TContext context = new TContext();
      TMessage msg = new TMessage<String>();
      msg.Body = "Hello";
    }
}
