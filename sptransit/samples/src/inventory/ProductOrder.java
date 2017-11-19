package inventory;

public class ProductOrder {

    private int _orderId;
    private String _product;
    private String _username;
    private int _quantity;

    public ProductOrder(int orderId, String product, String username, int quantity) {
        _orderId = orderId;
        _product = product;
        _username = username;
        _quantity = quantity;
    }

    public int getOrderId() {
        return _orderId;
    }

    public String getProduct() {
        return _product;
    }

    public String getUsername() {
        return _username;
    }

    public int getQuantity() {
        return _quantity;
    }
}
