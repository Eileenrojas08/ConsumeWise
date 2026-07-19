package smartfoodmanager.db;

import smartfoodmanager.model.FoodItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    public void insert(FoodItem item) {
        String sql = "INSERT INTO food_items " +
                     "(name, quantity, unit, category, expiration_date, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getQuantity());
            ps.setString(3, item.getUnit());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getExpirationDate());
            ps.setString(6, item.getImagePath());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    public List<FoodItem> getAll() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items ORDER BY expiration_date";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new FoodItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("category"),
                        rs.getString("expiration_date"),
                        rs.getString("image_path")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Read failed: " + e.getMessage());
        }
        return items;
    }

    public void update(FoodItem item) {
        String sql = "UPDATE food_items SET " +
                     "name=?, quantity=?, unit=?, category=?, expiration_date=?, image_path=? " +
                     "WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getQuantity());
            ps.setString(3, item.getUnit());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getExpirationDate());
            ps.setString(6, item.getImagePath());
            ps.setInt(7, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM food_items WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }
}