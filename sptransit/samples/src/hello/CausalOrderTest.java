package hello;

import sptransit.CausalSocket;

import java.io.IOException;
import java.util.Scanner;

public class CausalOrderTest {
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
            int servercount = 3;
            switch (num) {
                case 1:
                    log = HelloLogger.setup("server1");

                    CausalSocket causalSocket1 = new CausalSocket(log, servercount);
                    causalSocket1.bind("localhost", 6000);

                    System.out.println("Waiting for all servers to come online");
                    //Thread.sleep(500);
                    System.out.println("Press enter to continue once all server are up");
                    System.in.read();

                    causalSocket1.connect("localhost", 6001);
                    causalSocket1.send("Hi from p1 -> p2");

                    causalSocket1.connect("localhost", 6002);
                    causalSocket1.send("Hi from p1 -> p3");

                    break;
                case 2:
                    log = HelloLogger.setup("server2");

                    CausalSocket causalSocket2 = new CausalSocket(log, servercount);
                    causalSocket2.bind("localhost", 6001);

                    System.out.println("Waiting for all servers to come online");
                    //Thread.sleep(500);
                    System.out.println("Press enter to continue once all server are up");
                    System.in.read();

                    causalSocket2.connect("localhost", 6002);
                    causalSocket2.send("Hi from p2 -> p3");

                    System.out.println("Received +++ " +causalSocket2.receive());

                    causalSocket2.connect("localhost", 6002);
                    causalSocket2.send("Hi from p2 -> p3");

                    System.out.println("Received +++ " +causalSocket2.receive());

                    break;
                case 3:
                    log = HelloLogger.setup("server3");

                    CausalSocket causalSocket3 = new CausalSocket(log, servercount);
                    causalSocket3.bind("localhost", 6002);

                    System.out.println("Waiting for all servers to come online");
                    Thread.sleep(500);
                    System.out.println("Press enter to continue once all server are up");
                    System.in.read();

                    System.out.println("Received +++ " +causalSocket3.receive());
                    System.out.println("Received +++ " +causalSocket3.receive());
                    System.out.println("Received +++ " +causalSocket3.receive());

                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Press enter to exit");
        in.nextLine();
    }
}
