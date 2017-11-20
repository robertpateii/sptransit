package sptransit;

public class TPacket<E> implements java.io.Serializable {
    private TMessage<E> _message;
    private TAddress _address;

    public TPacket(TMessage<E> message, TAddress address) {
        _message = message;
        _address = address;
    }

    public TMessage<E> get_message() {
        return _message;
    }

    public void set_message(TMessage<E> _message) {
        this._message = _message;
    }

    public TAddress get_address() {
        return _address;
    }

    public void set_address(TAddress _address) {
        this._address = _address;
    }
}
