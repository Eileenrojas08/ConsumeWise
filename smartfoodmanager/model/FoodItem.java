package smartfoodmanager.model;

public class FoodItem {

    private int id;
    private String name;
    private int quantity;
    private String unit;
    private String category;
    private String expirationDate;
    private String imagePath;

    public FoodItem(String name, int quantity, String unit, String category,
                    String expirationDate, String imagePath) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.expirationDate = expirationDate;
        this.imagePath = imagePath;
    }

    public FoodItem(int id, String name, int quantity, String unit, String category,
                    String expirationDate, String imagePath) {
        this(name, quantity, unit, category, expirationDate, imagePath);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}