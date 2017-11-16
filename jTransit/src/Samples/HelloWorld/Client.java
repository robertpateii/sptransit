package Samples.HelloWorld;
import jTransit.*;

public class Client {
    public static void main (String[] args) {
      jTContext context = new jTContext();
      jTMessage msg = new jTMessage<String>();
      msg.Body = "Hello";
    }
}
