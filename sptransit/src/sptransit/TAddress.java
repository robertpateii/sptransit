package sptransit;

public class TAddress implements java.io.Serializable {
    private String _ipaddress;
    private Integer _port;

    public TAddress(String address, Integer port) {
        this._ipaddress = address;
        this._port = port;
    }

    public String get_ipaddress() {
        return _ipaddress;
    }

    public Integer get_port() {
        return _port;
    }
}
