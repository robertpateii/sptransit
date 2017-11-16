package jTransit;

public class jTMessage<E> {
    private E _body;

    public  jTMessage(E body)
    {
        _body = body;
    }

    public E getBody(){return _body;}

    public void setBody(E body){_body=body;}
}
