
package reservation;


import java.net.InetSocketAddress;
import java.net.Socket;

public class CSRequest {
    private int _timeStamp;
    private int _pid;

    public  CSRequest(int pid, int timeStamp)
    {
        _pid=pid;
        _timeStamp = timeStamp;
    }

   public  int get_timeStamp(){
        return _timeStamp;
   }

    public int get_pid(){
       return _pid;
    }
}
