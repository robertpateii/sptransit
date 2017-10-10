import java.net.InetSocketAddress;

public class CSRequest {
    private int _timeStamp;
    private int _pid;
    private InetSocketAddress _address;
    private String _command;

    public  CSRequest(int pid,InetSocketAddress address,int timeStamp,String command)
    {
        _pid=pid;
        _address = address;
        _timeStamp = timeStamp;
        _command = command;
    }

   public  int get_timeStamp(){
        return _timeStamp;
   }

    public InetSocketAddress get_address() {
        return _address;
    }

    public String get_command() {
        return _command;
    }

    public int get_pid(){
       return _pid;
    }
}
