package sptransit;

public class TMessage<E> {
    private E _body;
    private String _ipAddress;
    private int _port;

    public TMessage(E body) {
        _body = body;
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

