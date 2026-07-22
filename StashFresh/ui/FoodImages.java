package StashFresh.ui;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class FoodImages {

    private static final Map<String, Image> CACHE = new HashMap<>();

    public static Image get(String name) {
        return CACHE.computeIfAbsent(name, n -> {
            try {
                return new Image("file:images/" + n + ".png");
            } catch (Exception e) {
                System.out.println("Missing image: " + n);
                return null;
            }
        });
    }
}