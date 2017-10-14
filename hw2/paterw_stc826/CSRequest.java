
public class CSRequest implements java.io.Serializable {

    private Timestamp _timeStamp; // includes pid unique to each server
    private String _command;

    public static CSRequest Parse(String message) {
        return new CSRequest(message);
    }

    public CSRequest(Timestamp timeStamp, String command) {
        _timeStamp = timeStamp;
        _command = command;
    }

    public CSRequest(String command)
    {
        _timeStamp = new Timestamp(command);
    }

    public Timestamp get_timeStamp() {
        return _timeStamp;
    }

    public String get_command() {
        return _command;
    }
}
