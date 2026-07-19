package smartfoodmanager.model;

public class ShoppingItem {

    private int id;
    private String itemName;
    private int quantity;

    public ShoppingItem(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public ShoppingItem(int id, String itemName, int quantity) {
        this(itemName, quantity);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}