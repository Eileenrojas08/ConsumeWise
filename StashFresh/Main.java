package StashFresh;

import java.util.List;

import StashFresh.db.DatabaseManager;
import StashFresh.db.FoodItemDAO;
import StashFresh.model.FoodItem;

/**
 * Temporary test driver. NO GUI. Its only job today is to prove the
 * database + model + CRUD all work end to end. Once this prints correctly,
 * you KNOW the whole foundation is solid, and the GUI (Swing or JavaFX)
 * just becomes buttons that call these same DAO methods.
 */
public class Main {
    public static void main(String[] args) {

        // 1. Build the tables (safe to run every time)
        DatabaseManager.createTables();

        FoodItemDAO dao = new FoodItemDAO();

        // 2. CREATE — add a couple of items
        dao.insert(new FoodItem("Milk", 1, "carton", "Dairy", "2026-07-10", null));
        dao.insert(new FoodItem("Spinach", 1, "bag", "Produce", "2026-07-07", null));
        dao.insert(new FoodItem("Eggs", 12, "count", "Dairy", "2026-07-20", null));

        // 3. READ — should print sorted by expiry (Spinach first, it dies soonest)
        System.out.println("\n--- Current fridge ---");
        List<FoodItem> items = dao.getAll();
        for (FoodItem item : items) {
            System.out.println(item);
        }

        // 4. UPDATE — bump the milk quantity, using the id of the first item read
        if (!items.isEmpty()) {
            FoodItem first = items.get(0);
            first.setQuantity(first.getQuantity() + 2);
            dao.update(first);
            System.out.println("\nUpdated: " + first.getName() + " -> qty " + first.getQuantity());
        }

        // 5. DELETE — remove that same item to confirm delete works
        if (!items.isEmpty()) {
            dao.delete(items.get(0).getId());
            System.out.println("Deleted item id " + items.get(0).getId());
        }

        // 6. READ again to confirm the changes stuck
        System.out.println("\n--- After update + delete ---");
        for (FoodItem item : dao.getAll()) {
            System.out.println(item);
        }
    }
}
