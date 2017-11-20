package sptransit;

public class TMessage<E> implements java.io.Serializable {
    private E _body;

    public TMessage(E body) {
        _body = body;
    }

    public E getBody() {
        return _body;
    }

    public void setBody(E body) {
        _body = body;
    }
}

