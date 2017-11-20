package sptransit;

public class TMessage<E> implements java.io.Serializable {
    private E _body;
    private String _ipAddress;
    private int _port;

    public TMessage(E body) {
        _body = body;
    }

    public TMessage(E body, TAddress addy) {
        _body = body;
        _ipAddress = addy.address;
        _port = addy.port;
    }
    public E getBody() {
        return _body;
    }

    public void setBody(E body) {
        _body = body;
    }

    public void setSourceAddress(String ipAddress, int port) {
        _ipAddress = ipAddress;
        _port = port;
    }

    public String getIpAddress() {
        return _ipAddress;
    }

    public int getPort() {
        return _port;
    }
}

