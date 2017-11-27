package hello;

import sptransit.PushSocket;

import java.io.IOException;
import java.util.Scanner;

public class CausalOrderCounterTest {
    private static java.util.logging.Logger log;

    /*
    this is a sample code for cause order, I couldn't figure out the command line run so
    to test run three different executions and enter 1,2,3 then once all are up hit continue on all
    the command line magic should stream line this
    **/
    public static void main(String[] args) throws IOException {
        System.out.println("Enter Server # to start");
        Scanner in = new Scanner(System.in);
        int num = in.nextInt();
        try {
            switch (num) {
                case 1:
                    log = HelloLogger.setup("server1");

                    PushSocket pushSocket1 = new PushSocket(log);
                    pushSocket1.bind("localhost", 6000);

                    System.out.println("Waiting for all servers to come online");
                    System.out.println("Press enter to continue once all server are up");
                    System.in.read();


                    // the problem is this gets to p3 after p1->p2 and p2->p3
                    // the tcp socket in bind will delay this for us
                    pushSocket1.send("Hi from p1 -> p3 (causally first for p3)", "localhost", 6002);

                    pushSocket1.send("Hi from p1 -> p2", "localhost", 6001);

                    break;
                case 2:
                    log = HelloLogger.setup("server2");

                    PushSocket pushSocket2 = new PushSocket(log);
                    pushSocket2.bind("localhost", 6001);

                    System.out.println("Received +++ " +pushSocket2.receive());

                    // the problem is this gets to p3 before p1s message to p3
                    pushSocket2.send("Hi from p2 -> p3 (causally second for p3)", "localhost", 6002);

                    System.out.println("Press enter to send p2's second message to p3");
                    System.in.read();

                    pushSocket2.send("Hi from p2 -> p3", "localhost", 6002);

                    System.out.println("Received +++ " +pushSocket2.receive());

                    break;
                case 3:
                    log = HelloLogger.setup("server3");

                    PushSocket pushSocket3 = new PushSocket(log);
                    pushSocket3.bind("localhost", 6002);

                    System.out.println("Received +++ " +pushSocket3.receive());
                    System.out.println("Received +++ " +pushSocket3.receive());
                    System.out.println("Received +++ " +pushSocket3.receive());

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Press enter to exit");
        in.nextLine();
    }
}
