// Cart.java
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private static class CartItem {
        private final Item item;
        private final int quantity;

        public CartItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public double lineTotal() {
            return item.getPreco() * quantity;
        }

        @Override
        public String toString() {
            return item.toString() + " x " + quantity + " = " + String.format("%.2f", lineTotal());
        }
    }

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Item item, int quantity) {
        items.add(new CartItem(item, quantity));
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::lineTotal).sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cart Contents:\n");
        for (CartItem ci : items) {
            sb.append(ci).append("\n");
        }
        sb.append("Total: ").append(String.format("%.2f", getTotal()));
        return sb.toString();
    }

    // Test fictício
    public static void main(String[] args) throws Exception {
        DatabaseInterface di = new DatabaseInterface();
        var items = di.listarItems();
        Cart cart = new Cart();
        if (items.size() > 2) {
            cart.addItem(items.get(0), 2);
            cart.addItem(items.get(2), 1);
        }
        System.out.println(cart);
    }
}
