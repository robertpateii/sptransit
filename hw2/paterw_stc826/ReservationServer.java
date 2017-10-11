
package reservation;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ReservationServer
{
    private int _id;
    private InetSocketAddress _address;
    private boolean _isOnline;

    public ReservationServer(int id, InetSocketAddress address,boolean isOnline)
    {
        _id = id;
        _address = address;
        _isOnline = isOnline;
    }

    public int getId()
    {
        return _id;
    }

    public InetSocketAddress getAddress()
    {
        return _address;
    }
}