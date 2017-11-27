package hello;

import sptransit.CausalParticipant;
import sptransit.CausalSocket;

import java.io.IOException;
import java.util.ArrayList;
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

        //these can be read from a file or any source
        ArrayList<CausalParticipant> participants = new ArrayList<>();
        participants.add(new CausalParticipant("localhost",6000,0));
        participants.add(new CausalParticipant("localhost",6001,1));
        participants.add(new CausalParticipant("localhost",6002,2));

        int num = in.nextInt();
        try {
            int servercount = 3;
            switch (num) {
                case 1:
                    log = HelloLogger.setup("server1");

                    CausalSocket causalSocket1 = new CausalSocket(log, servercount,participants);
                    causalSocket1.bind("localhost", 6000);

                    System.out.println("Waiting for all servers to come online");
                    System.out.println("Press enter to continue once all server are up");
                    System.in.read();


                    causalSocket1.connect("localhost", 6002);
                    // the problem is this gets to p3 after p1->p2 and p2->p3
                    causalSocket1.send("Hi from p1 -> p3 (causally first for p3)");


                    causalSocket1.connect("localhost", 6001);
                    causalSocket1.send("Hi from p1 -> p2");

                    break;
                case 2:
                    log = HelloLogger.setup("server2");

                    CausalSocket causalSocket2 = new CausalSocket(log, servercount,participants);
                    causalSocket2.bind("localhost", 6001);

                    System.out.println("Received +++ " +causalSocket2.receive());

                    causalSocket2.connect("localhost", 6002);
                    // the problem is this gets to p3 before p1s message to p3
                    causalSocket2.send("Hi from p2 -> p3 (causally second for p3)");

                    System.out.println("Press enter to send p2's second message to p3");
                    System.in.read();

                    causalSocket2.connect("localhost", 6002);
                    causalSocket2.send("Hi from p2 -> p3");

                    System.out.println("Received +++ " +causalSocket2.receive());

                    break;
                case 3:
                    log = HelloLogger.setup("server3");

                    CausalSocket causalSocket3 = new CausalSocket(log, servercount,participants);
                    causalSocket3.bind("localhost", 6002);

                    System.out.println("Received +++ " +causalSocket3.receive());
                    System.out.println("Received +++ " +causalSocket3.receive());
                    System.out.println("Received +++ " +causalSocket3.receive());

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Press enter to exit");
        in.nextLine();
    }
}
