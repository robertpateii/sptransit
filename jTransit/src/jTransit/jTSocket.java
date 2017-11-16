package jTransit;

public class jTSocket {
    private jTContext _jTContext;
    private String _bindEndPoint;
    private String _connectEndPoint;

    public  jTSocket(jTContext jtcontext)
    {
        _jTContext = jtcontext;
    }

    public void bind(String endpoint){
        _bindEndPoint = endpoint;
    }

    public void connect(String endpoint){
        _connectEndPoint = endpoint;
    }

    public void Send(jTMessage message) {}

    public jTMessage Receive(){ throw new java.lang.UnsupportedOperationException("Not supported yet.");} //generics response.get<Person>() as type Person
}

