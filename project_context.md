# Smart Food Manager — Project Context File
> Give this file to any AI assistant before asking for help with this project.

---

## What This Is
A JavaFX + SQLite desktop application built in NetBeans for a Java Programming final project (COP2800-2265-9721). The student is Eileen, a college student with beginner-intermediate Java experience.

---

## Assignment Requirements (non-negotiable)
- **GUI:** JavaFX (NOT Swing/JFrame — those won't satisfy the requirement)
- **Database:** SQLite
- **CRUD:** Create, Read, Update, Delete must all work on at least one table
- **Input validation + exception handling** required throughout
- **OOP principles** required — proper packages, classes, encapsulation
- **Final deliverables:** source code, GitHub repo link, README, database file, 5-minute demo video
- **Bugs/crashes/incomplete features = point deductions**
- AI tools are explicitly encouraged by the instructor

---

## Project: Smart Food Manager

### Elevator pitch
A desktop app that tracks food inventory, warns you before things expire (color-coded), matches expiring ingredients to pre-loaded recipes, and includes a shopping list. Login is PIN-based.

### Major features
1. **PIN login** — users log in with a username + PIN. PIN is stored hashed (SHA-256), not plaintext.
2. **Food inventory** — add, edit, delete food items with name, quantity, unit, category, and expiration date
3. **Color-coded expiry indicators** — green (fresh), yellow (expiring soon), red (expired or today)
4. **Recipe matching** — compares items in fridge against recipe ingredient lists; surfaces recipes you can make with what's about to expire
5. **Shopping list** — add items you're out of; manage it separately from inventory

---

## Database: SQLite (file: `smartfoodmanager.db`)

### Tables

```sql
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  pin_hash TEXT NOT NULL,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS food_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER,
  name TEXT NOT NULL,
  quantity INTEGER NOT NULL,
  unit TEXT,
  category TEXT,
  expiration_date TEXT,   -- stored as 'YYYY-MM-DD'
  image_path TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS recipes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  instructions TEXT,
  servings INTEGER
);

CREATE TABLE IF NOT EXISTS recipe_ingredients (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  recipe_id INTEGER NOT NULL,
  ingredient_name TEXT NOT NULL,
  amount_needed TEXT,
  FOREIGN KEY (recipe_id) REFERENCES recipes(id)
);

CREATE TABLE IF NOT EXISTS shopping_list (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER,
  item_name TEXT NOT NULL,
  quantity INTEGER,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Key relationships
- `users` 1 → many `food_items` (via `user_id`) — each user has their own fridge
- `users` 1 → many `shopping_list` (via `user_id`)
- `recipes` 1 → many `recipe_ingredients` (via `recipe_id`)
- Recipe matching is done in Java code by comparing `recipe_ingredients.ingredient_name` against `food_items.name` (not an enforced FK)

---

## Package Structure

```
src/
  smartfoodmanager/
    Main.java                  ← app entry point, calls DatabaseManager.createTables()
    db/
      DatabaseManager.java     ← getConnection(), createTables()
      FoodItemDAO.java         ← CRUD for food_items (insert, getAll, update, delete)
    model/
      FoodItem.java            ← model class for one food_items row
    ui/                        ← JavaFX controllers go here (not built yet)
```

### Files already written
- `DatabaseManager.java` ✅
- `FoodItem.java` ✅
- `FoodItemDAO.java` ✅
- `Main.java` ✅ (currently a test harness with no GUI — prints CRUD results to console)

### Files still needed
- `UserDAO.java` — login, create user, hash PIN
- `RecipeDAO.java` — insert/read recipes and ingredients
- `ShoppingListDAO.java` — CRUD for shopping list
- `RecipeMatcher.java` — logic to match fridge contents against recipes
- All JavaFX UI controllers and FXML files

---

## Tech Stack
- **Language:** Java (beginner-intermediate level — keep explanations clear)
- **GUI:** JavaFX
- **Database:** SQLite via `sqlite-jdbc` JAR (added to project libraries manually)
- **IDE:** NetBeans (Java with Ant project)
- **PIN hashing:** SHA-256 via `java.security.MessageDigest`
- **No build tools** (no Maven, no Gradle — plain Ant/NetBeans project)

---

## Code Style Preferences
- Clear variable names, no abbreviations without explanation
- Comments on anything non-obvious
- `try-catch` exception handling (not `throws` declarations on main methods)
- `PreparedStatement` with `?` placeholders for all SQL (never string concatenation)
- Keep classes focused — one job per class

---

## What NOT to do
- Do NOT use Swing or JFrame — JavaFX only for UI
- Do NOT store the raw PIN — always hash it first
- Do NOT use Maven/Gradle — this is a plain NetBeans Ant project
- Do NOT skip exception handling — the assignment explicitly requires it
- Do NOT put SQL logic inside model classes — keep it in the DAO layer

---

## Current Status (as of session start)
- Project proposal submitted ✅
- Database schema designed ✅
- Core files written (DatabaseManager, FoodItem, FoodItemDAO, Main) ✅
- NetBeans project being set up now
- SQLite JAR not yet added to project
- No JavaFX UI built yet
- Next immediate step: get Main.java running in NetBeans to confirm CRUD works end to end