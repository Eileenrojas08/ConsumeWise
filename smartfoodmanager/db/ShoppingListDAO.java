package smartfoodmanager.db;

import smartfoodmanager.model.ShoppingItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListDAO {

    public void insert(ShoppingItem item) {
        String sql = "INSERT INTO shopping_list (item_name, quantity) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Shopping list insert failed: " + e.getMessage());
        }
    }

    public List<ShoppingItem> getAll() {
        List<ShoppingItem> results = new ArrayList<>();
        String sql = "SELECT * FROM shopping_list ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(new ShoppingItem(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            System.out.println("Shopping list read failed: " + e.getMessage());
        }
        return results;
    }

    public void delete(int id) {
        String sql = "DELETE FROM shopping_list WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Shopping list delete failed: " + e.getMessage());
        }
    }
}