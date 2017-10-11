import java.net.InetSocketAddress;
import java.net.Socket;

public class CSRequest {
    private int _timeStamp;
    private int _pid;
    private Socket _clientSocket;
    private String _command;

    public  CSRequest(int pid, Socket clientSocket, int timeStamp, String command)
    {
        _pid=pid;
        _clientSocket = clientSocket;
        _timeStamp = timeStamp;
        _command = command;
    }

   public  int get_timeStamp(){
        return _timeStamp;
   }

    public Socket get_clientSocket() {
        return _clientSocket;
    }

    public String get_command() {
        return _command;
    }

    public int get_pid(){
       return _pid;
    }
}
