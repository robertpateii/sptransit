package sptransit;

public class CausalParticipant extends TAddress {
    private int pid;
    public CausalParticipant(String host,int port,int pid)
    {
        super(host,port);
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public int hashCode()
    {
        return (this.getIPAddress()+this.getPort()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        TAddress address = (TAddress) obj;
        return address.getIPAddress() == this.getIPAddress()
                && address.getPort() == this.getPort();
    }
}
