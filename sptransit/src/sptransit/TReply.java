package sptransit;

public class TReply<E> {

    private E body;

    public TReply(E body) {
        this.body = body;
    }

    public E getBody() {
        return body;
    }
}
