package sptransit;

// This is only used within this package so don't make it public
class TAddress implements java.io.Serializable {
    private String ipaddress;
    private Integer port;

    public TAddress(String address, Integer port) {
        this.ipaddress = address;
        this.port = port;
    }

    public String getIPAddress() {
        return ipaddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
