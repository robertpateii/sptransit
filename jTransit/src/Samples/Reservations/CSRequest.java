package Samples.Reservations;

public class CSRequest implements java.io.Serializable {

    private Timestamp _timeStamp; // includes pid unique to each server
    private String _command;

    public CSRequest(int pid, int clock) {
        
        _timeStamp = new Timestamp(clock, pid);
    }

    public CSRequest(Timestamp timeStamp, String command) {
        _timeStamp = timeStamp;
        _command = command;
    }

    public Timestamp get_timeStamp() {
        return _timeStamp;
    }

    public String get_command() {
        return _command;
    }
}
