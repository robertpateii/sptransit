package Samples.InventoryManagement;
import java.util.*;

public class InventoryManager {
    private static Map<String, Integer> productInventory;
    private static ArrayList<ProductOrder> orders;
    private static int orderIdCounter;

    public static void Initialize(List<String> lines) {

        orders = new ArrayList<ProductOrder>();
        productInventory = new HashMap<String, Integer>();
        lines.forEach((String line) -> {
            if (line.length() > 0 && line.contains(" ")) {
                String productName = line.substring(0, line.indexOf(" "));
                int count = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
                productInventory.put(productName, count);
            } else {
                System.out.println("This line was empty or had no space: " + line);
            }
        });
        orderIdCounter = 1;
    }

    public static String HandleCommand(String command) {
        String[] options = command.split(" ");
        String commandType = options[0].toLowerCase();
        switch (commandType) {
            case "purchase":
                return orderPurchase(options);
            case "cancel":
                return orderCancel(options);
            case "search":
                return userSearch(options);
            case "list":
                return getList(options);
            default:
                return "Invalid command type: " + commandType;
        }
    }

    private synchronized static String orderPurchase(String[] options) {
        String username = options[1];
        String product = options[2];
        int quantity = Integer.parseInt(options[3]);

        if (!productInventory.containsKey(product)) {
            return "Not Available - We do not sell this product";
        }

        if (productInventory.get(product) < quantity) {
            return "Not Available - Not enough items";
        }

        orderIdCounter++;
        productInventory.put(product, productInventory.get(product) - quantity);
        orders.add(new ProductOrder(orderIdCounter, product, username, quantity));
        return "Your order has been placed, " + orderIdCounter + " " + username + " " + product + " " + quantity;
    }

    private synchronized static String orderCancel(String[] options) {
        int orderid = Integer.parseInt((options[1]));
        ProductOrder order = findByOrderId(orderid);
        if (order == null) {
            return orderid + " not found, no such order";
        }

        productInventory.put(order.getProduct(), productInventory.get(order.getProduct()) + order.getQuantity());
        orders.remove(order);
        return "Order " + orderid + " is canceled";
    }

    private synchronized static String userSearch(String[] options) {
        String userName = options[1];
        String orderList = "";
        for (int i = 0; i < orders.size(); i++) {
            if (((ProductOrder) orders.get(i)).getUsername().equals(userName)) {
                orderList += "\n " + ((ProductOrder) orders.get(i)).getOrderId()
                        + ", "
                        + ((ProductOrder) orders.get(i)).getProduct() + ", "
                        + ((ProductOrder) orders.get(i)).getQuantity();
            }
        }
        return orderList;
    }

    private synchronized static String getList(String[] options) {
        String list;
        Set<String> keys = productInventory.keySet();
        Iterator<String> iterator = keys.iterator();
        if (iterator.hasNext()) {
            String product = iterator.next();
            int count = productInventory.get(product);
            list = product + " " + count;
            while (iterator.hasNext()) {
                product = iterator.next();
                count = productInventory.get(product);
                // I tried to get the System.lineSeparator to work but no luck
                list += "-" + product + " " + count;
            }
            return list;
        } else {
            return "No products found.";
        }
    }

    private static ProductOrder findByOrderId(int orderId) {
        for (int i = 0; i < orders.size(); i++) {
            if (((ProductOrder) orders.get(i)).getOrderId() == orderId) {
                return (ProductOrder) orders.get(i);
            }
        }
        return null;
    }

}
