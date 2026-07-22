package StashFresh.ui;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class FoodTile extends StackPane {

    public static final String ACCENT = "#6FD69B";

    private final String foodName;
    private boolean selected = false;
    private final Region tint;
    private final StackPane check;

    public FoodTile(String foodName, Image image) {
        this.foodName = foodName;

        getStyleClass().add("food-card");
        setPrefSize(210, 200);                            

        // card body — top padding leaves room for the overhanging image
        Label label = new Label(foodName);
        label.getStyleClass().add("food-card-label");

        VBox body = new VBox(label);
        body.setAlignment(Pos.BOTTOM_CENTER);
        body.setPadding(new Insets(45, 12, 16, 12));

        // the food image, floating above the tile's top edge
        ImageView img = new ImageView(image);
        img.setFitWidth(90);
        img.setPreserveRatio(true);
        img.setMouseTransparent(true);
        img.setTranslateY(-25);
        img.setEffect(new DropShadow(16, 0, 6, Color.rgb(0, 0, 0, 0.6)));
        StackPane.setAlignment(img, Pos.TOP_CENTER);

        // mint tint, hidden until selected
        tint = new Region();
        tint.setStyle("-fx-background-color: rgba(111,214,155,0.14);"
                    + "-fx-background-radius: 16;");
        tint.setVisible(false);
        tint.setMouseTransparent(true);


        // checkmark badge, top-right
        Label tick = new Label("\u2713");
        tick.setStyle("-fx-text-fill: #10231A; -fx-font-weight: bold; -fx-font-size: 14px;");
        check = new StackPane(tick);
        check.setStyle("-fx-background-color: " + ACCENT + "; -fx-background-radius: 50;");
        check.setMinSize(26, 26);
        check.setMaxSize(26, 26);
        check.setVisible(false);
        check.setMouseTransparent(true);
        StackPane.setAlignment(check, Pos.TOP_RIGHT);
        StackPane.setMargin(check, new Insets(10, 10, 0, 0));

        getChildren().addAll(tint, body, img, check);

        setOnMousePressed(e -> playRipple(e.getX(), e.getY()));
        setOnMouseReleased(e -> toggle());
    }

    private void playRipple(double x, double y) {
        Circle ripple = new Circle(0, Color.web("#FFFFFF", 0.22));
        ripple.setManaged(false);
        ripple.setMouseTransparent(true);
        ripple.setCenterX(x);
        ripple.setCenterY(y);
        getChildren().add(ripple);

        double maxRadius = Math.hypot(getWidth(), getHeight());

        Timeline grow = new Timeline(new KeyFrame(Duration.millis(420),
                new KeyValue(ripple.radiusProperty(), maxRadius, Interpolator.EASE_OUT),
                new KeyValue(ripple.opacityProperty(), 0)));
        grow.setOnFinished(f -> getChildren().remove(ripple));
        grow.play();
    }

    private void toggle() {
        selected = !selected;
        tint.setVisible(selected);
        check.setVisible(selected);
        setEffect(selected ? new DropShadow(14, Color.web(ACCENT, 0.55)) : null);
    }

    public boolean isSelected() { return selected; }

    public String getFoodName() { return foodName; }
}