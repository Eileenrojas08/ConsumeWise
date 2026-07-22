package StashFresh.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class PickFoodScreen {

    private final Runnable onNext;

    public PickFoodScreen(Runnable onNext) {
        this.onNext = onNext;
    }

    private final List<FoodTile> tiles = new ArrayList<>();

    public BorderPane build() {
        String[] foods = { "apple", "banana", "orange" };

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(58);
        grid.setPadding(new Insets(60, 24, 24, 24));

        int col = 0, row = 0;
        for (String food : foods) {
            FoodTile tile = new FoodTile(capitalize(food), FoodImages.get(food));
            tiles.add(tile);
            grid.add(tile, col, row);
            if (++col == 4) { col = 0; row++; }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);

        Button next = new Button("Next");
        next.getStyleClass().add("btn-primary");

        next.setOnAction(e -> {
            System.out.println("Selected: " + getSelected());
            onNext.run();
        });

        HBox footer = new HBox(next);
        footer.setPadding(new Insets(16, 24, 24, 24));
        footer.setStyle("-fx-alignment: center-right;");

        BorderPane root = new BorderPane();
        root.setCenter(scroll);
        root.setBottom(footer);
        root.setStyle("-fx-background-color: #14181A;");
        return root;
    }

    public List<String> getSelected() {
        List<String> picked = new ArrayList<>();
        for (FoodTile t : tiles) {
            if (t.isSelected()) picked.add(t.getFoodName());
        }
        return picked;
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}