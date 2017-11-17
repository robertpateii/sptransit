package reservations;

public class LamportClock {

    /* sam tuesday stuff
    private int getLogicalClock(int requestLogicalClock)
    {
        logicalClock++;
        if(logicalClock<requestLogicalClock)
            logicalClock = requestLogicalClock+1;

        return logicalClock;
    }
     */

    int c;

    public LamportClock() {
        c = 1;
    }

    public int getValue() {
        return c;
    }

    public void tick() { // on internal events
        c = c + 1;
    }

    public void sendAction() {
        // include c in message
        c = c + 1;
    }

    public void receiveAction(int src, int sentValue) {
        c = Math.max(c, sentValue) + 1;
    }
}
