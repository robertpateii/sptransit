
import java.net.Socket;

public class CSRequest {

    private Timestamp _timeStamp; // includes pid unique to each server
    private Socket _clientSocket;
    private String _command;

    public static CSRequest Parse(String message, Socket pipe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CSRequest(Socket clientSocket, Timestamp timeStamp, String command) {
        _clientSocket = clientSocket;
        _timeStamp = timeStamp;
        _command = command;
    }

    public Timestamp get_timeStamp() {
        return _timeStamp;
    }

    public Socket get_clientSocket() {
        return _clientSocket;
    }

    public String get_command() {
        return _command;
    }
}
