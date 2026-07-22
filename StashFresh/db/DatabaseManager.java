package StashFresh.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:smartfoodmanager.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createTables() {
        String users =
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT NOT NULL UNIQUE," +
            "  pin_hash TEXT NOT NULL," +
            "  created_at TEXT" +
            ")";

        String foodItems =
            "CREATE TABLE IF NOT EXISTS food_items (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id INTEGER," +
            "  name TEXT NOT NULL," +
            "  quantity INTEGER NOT NULL," +
            "  unit TEXT," +
            "  category TEXT," +
            "  expiration_date TEXT," +
            "  image_path TEXT," +
            "  FOREIGN KEY (user_id) REFERENCES users(id)" +
            ")";

        String recipes =
            "CREATE TABLE IF NOT EXISTS recipes (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT NOT NULL," +
            "  instructions TEXT," +
            "  servings INTEGER" +
            ")";

        String recipeIngredients =
            "CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  recipe_id INTEGER NOT NULL," +
            "  ingredient_name TEXT NOT NULL," +
            "  amount_needed TEXT," +
            "  FOREIGN KEY (recipe_id) REFERENCES recipes(id)" +
            ")";

        String shoppingList =
            "CREATE TABLE IF NOT EXISTS shopping_list (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  user_id INTEGER," +
            "  item_name TEXT NOT NULL," +
            "  quantity INTEGER," +
            "  FOREIGN KEY (user_id) REFERENCES users(id)" +
            ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(users);
            stmt.execute(foodItems);
            stmt.execute(recipes);
            stmt.execute(recipeIngredients);
            stmt.execute(shoppingList);
            System.out.println("Tables ready.");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }

        seedSampleRecipesIfNeeded();
    }

    private static void seedSampleRecipesIfNeeded() {

        try (Connection conn = getConnection();
             Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) AS total FROM recipes")) {
            rs.next();
            if (rs.getInt("total") > 0) {
                return;
            }
        } catch (SQLException e) {
            System.out.println("Recipe check failed: " + e.getMessage());
            return;
        }

        String[][] recipeInfo = {
            {"Spinach & Cheddar Omelette", "Whisk the eggs. Saute the spinach until wilted. Fold in the cheddar. Cook over medium heat until set.", "2"},
            {"Bell Pepper Frittata", "Whisk eggs with a splash of milk. Stir in peppers and onion. Bake at 375F for 20 minutes until golden.", "4"},
            {"Avocado & Tomato Toast", "Toast the bread. Mash the avocado on top. Add sliced tomato, salt, and pepper.", "1"},
            {"Chicken & Veggie Stir-Fry", "Slice the chicken and vegetables. Stir-fry in a hot pan with oil, soy sauce, and ginger until cooked through.", "3"}
        };

        String[][] ingredientsForEachRecipe = {
            {"Eggs", "Spinach", "Cheddar", "Milk"},
            {"Eggs", "Bell Peppers", "Cheddar", "Milk", "Onion"},
            {"Avocado", "Tomato", "Bread"},
            {"Chicken Breast", "Bell Peppers", "Onion", "Carrots"}
        };

        try (Connection conn = getConnection()) {

            for (int i = 0; i < recipeInfo.length; i++) {

                String insertRecipeSql =
                        "INSERT INTO recipes (name, instructions, servings) VALUES (?, ?, ?)";

                int newRecipeId = -1;

                try (PreparedStatement insertRecipe = conn.prepareStatement(
                        insertRecipeSql, Statement.RETURN_GENERATED_KEYS)) {

                    insertRecipe.setString(1, recipeInfo[i][0]);
                    insertRecipe.setString(2, recipeInfo[i][1]);
                    insertRecipe.setInt(3, Integer.parseInt(recipeInfo[i][2]));
                    insertRecipe.executeUpdate();

                    try (ResultSet keys = insertRecipe.getGeneratedKeys()) {
                        if (keys.next()) {
                            newRecipeId = keys.getInt(1);
                        }
                    }
                }

                if (newRecipeId == -1) {
                    continue;
                }

                String insertIngredientSql =
                        "INSERT INTO recipe_ingredients (recipe_id, ingredient_name, amount_needed) " +
                        "VALUES (?, ?, ?)";

                for (String ingredientName : ingredientsForEachRecipe[i]) {
                    try (PreparedStatement insertIngredient =
                                 conn.prepareStatement(insertIngredientSql)) {
                        insertIngredient.setInt(1, newRecipeId);
                        insertIngredient.setString(2, ingredientName);
                        insertIngredient.setString(3, "as needed");
                        insertIngredient.executeUpdate();
                    }
                }
            }

            System.out.println("Sample recipes added.");

        } catch (SQLException e) {
            System.out.println("Recipe seeding failed: " + e.getMessage());
        }
    }
}