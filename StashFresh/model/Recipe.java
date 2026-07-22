package StashFresh.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {

    private int id;
    private String name;
    private String instructions;
    private int servings;
    private List<String> ingredients = new ArrayList<>();

    public Recipe(int id, String name, String instructions, int servings) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.servings = servings;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public int getServings() { return servings; }
    public List<String> getIngredients() { return ingredients; }
}