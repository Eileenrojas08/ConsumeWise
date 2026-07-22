package StashFresh.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import StashFresh.model.Recipe;

public class RecipeDAO {

    public List<Recipe> getAllWithIngredients() {
        Map<Integer, Recipe> recipesById = new LinkedHashMap<>();

        String sql = "SELECT r.id, r.name, r.instructions, r.servings, ri.ingredient_name " +
                     "FROM recipes r LEFT JOIN recipe_ingredients ri ON ri.recipe_id = r.id " +
                     "ORDER BY r.id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");

                Recipe recipe = recipesById.get(id);
                if (recipe == null) {
                    recipe = new Recipe(
                            id,
                            rs.getString("name"),
                            rs.getString("instructions"),
                            rs.getInt("servings"));
                    recipesById.put(id, recipe);
                }

                String ingredientName = rs.getString("ingredient_name");
                if (ingredientName != null) {
                    recipe.getIngredients().add(ingredientName);
                }
            }
        } catch (SQLException e) {
            System.out.println("Recipe read failed: " + e.getMessage());
        }

        return new ArrayList<>(recipesById.values());
    }
}