package sptransit;

public class TSocket {
    private sptransit.TContext _jTContext;
    private String _bindEndPoint;
    private String _connectEndPoint;

    public  TSocket (sptransit.TContext jtcontext)
    {
        _jTContext = jtcontext;
    }

    public void bind(String endpoint){
        _bindEndPoint = endpoint;
    }

    public void connect(String endpoint){
        _connectEndPoint = endpoint;
    }

    public void Send(sptransit.TMessage message) {}

    public sptransit.TMessage Receive(){ throw new java.lang.UnsupportedOperationException("Not supported yet.");} //generics response.get<Person>() as type Person
}

