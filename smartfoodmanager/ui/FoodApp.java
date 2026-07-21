package smartfoodmanager.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import java.io.File;


import smartfoodmanager.db.DatabaseManager;
import smartfoodmanager.db.FoodItemDAO;
import smartfoodmanager.db.RecipeDAO;
import smartfoodmanager.db.ShoppingListDAO;
import smartfoodmanager.model.FoodItem;
import smartfoodmanager.model.Recipe;
import smartfoodmanager.model.ShoppingItem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FoodApp extends Application {

    private static final int WARNING_DAYS = 3;

    private final FoodItemDAO foodDao = new FoodItemDAO();
    private final RecipeDAO recipeDao = new RecipeDAO();
    private final ShoppingListDAO shoppingDao = new ShoppingListDAO();

    private BorderPane root;
    private VBox sidebar;
    private boolean sidebarOpen = true;
    private final DoubleProperty sidebarWidth = new SimpleDoubleProperty(250);
    private static final double SIDEBAR_OPEN_WIDTH = 250;
    private static final double SIDEBAR_CLOSED_WIDTH = 0;

    private final List<Button> navButtons = new ArrayList<>();
    private Stage stage;
private StackPane rootStack;
private boolean screensaverShowing = false;
private static final int IDLE_SECONDS = 60;

@Override
public void start(Stage stage) {
    this.stage = stage;
    stage.setTitle("ConsumeWise");
    showStartupVideo();
}

private void showMainApp() {
    DatabaseManager.createTables();
    root = new BorderPane();
    buildSidebar();
    navigateTo(0);

    Button hamburger = new Button("\u2630");
    hamburger.getStyleClass().add("hamburger-btn");
    hamburger.setOnAction(e -> toggleSidebar());
    hamburger.translateXProperty().bind(sidebarWidth);

    rootStack = new StackPane(root, hamburger);
    StackPane.setAlignment(hamburger, Pos.TOP_LEFT);
    StackPane.setMargin(hamburger, new Insets(16, 0, 0, 16));

    Scene scene = new Scene(rootStack, 1220, 760);
    scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    stage.setScene(scene);
    stage.show();

    setupIdleTimer(scene);
}

private void showStartupVideo() {
    try {
        Media media = new Media(new File("video/startup.mp4").toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(1);
        player.setRate(1.5);   // speeds a 23s clip to ~15s

        MediaView view = new MediaView(player);
        view.setPreserveRatio(true);
        view.setFitWidth(1220);

        Label title = new Label("ConsumeWise");
        title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label tagline = new Label("Manage what you have. Use it before you lose it.");
        tagline.setStyle("-fx-font-size: 16px; -fx-text-fill: #EEEEEE;");
        Label hint = new Label("click to continue");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #BBBBBB;");

        VBox textCard = new VBox(10, title, tagline, hint);
        textCard.setAlignment(Pos.CENTER);
        textCard.setStyle("-fx-background-color: rgba(0,0,0,0.55);"
                        + "-fx-background-radius: 18; -fx-padding: 28 40 28 40;");
        textCard.setMaxWidth(560);
        textCard.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane splash = new StackPane(view, textCard);
        splash.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(splash, 1220, 760);
        stage.setScene(scene);
        stage.show();

        player.setOnEndOfMedia(() -> { player.stop(); showMainApp(); });
        splash.setOnMouseClicked(e -> { player.stop(); showMainApp(); });
        scene.setOnKeyPressed(e -> { player.stop(); showMainApp(); });

        player.play();
    } catch (Exception ex) {
        // if the video is missing or won't load, just open the app
        System.out.println("Startup video failed: " + ex.getMessage());
        showMainApp();
    }
}

private void setupIdleTimer(Scene scene) {
    PauseTransition idle = new PauseTransition(Duration.seconds(IDLE_SECONDS));
    idle.setOnFinished(e -> showScreensaver());
    scene.addEventFilter(InputEvent.ANY, e -> idle.playFromStart());
    scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
        if (e.getCode() == KeyCode.F5) showScreensaver();
    });
    idle.play();
}

private void showScreensaver() {
    if (screensaverShowing) return;
    try {
        Media media = new Media(new File("video/idle.mp4").toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);

        MediaView view = new MediaView(player);
        view.setPreserveRatio(true);
        view.fitWidthProperty().bind(rootStack.widthProperty());

        StackPane cover = new StackPane(view);
        cover.setStyle("-fx-background-color: black;");
        cover.setOnMouseClicked(e -> {
            player.stop();
            rootStack.getChildren().remove(cover);
            screensaverShowing = false;
        });

        screensaverShowing = true;
        rootStack.getChildren().add(cover);
        player.play();
    } catch (Exception ex) {
        System.out.println("Idle video failed: " + ex.getMessage());
        screensaverShowing = false;
    }
}

private void toggleSidebar() {
    double target = sidebarOpen ? SIDEBAR_CLOSED_WIDTH : SIDEBAR_OPEN_WIDTH;
    Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(250), new KeyValue(sidebarWidth, target))
    );
    timeline.play();
    sidebarOpen = !sidebarOpen;
}

    private void buildSidebar() {
        sidebar = new VBox(6);
        sidebar.getStyleClass().add("sidebar");
        sidebar.prefWidthProperty().bind(sidebarWidth);
        sidebar.minWidthProperty().bind(sidebarWidth);
        sidebar.maxWidthProperty().bind(sidebarWidth);
        Label logo = new Label("\uD83C\uDF43");
        logo.getStyleClass().add("logo-square");
        Label title = new Label("ConsumeWise");
        title.getStyleClass().add("sidebar-title");
        HBox logoRow = new HBox(10, logo, title);
        logoRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Manage what you have.\nUse it before you lose it.");
        subtitle.getStyleClass().add("sidebar-subtitle");
        subtitle.setWrapText(true);

        VBox logoBox = new VBox(8, logoRow, subtitle);
        logoBox.setPadding(new Insets(0, 0, 22, 2));

        createNavButton("\uD83C\uDFE0  Dashboard", this::showDashboardView);
        createNavButton("\uD83D\uDCE6  Inventory", this::showInventoryView);
        createNavButton("\uD83C\uDF73  Recipes", () -> showRecipesView(false));
        createNavButton("\u2B50  Recipe Matches", () -> showRecipesView(true));
        createNavButton("\uD83D\uDCC5  Expiration Center", this::showExpirationView);
        createNavButton("\uD83D\uDED2  Shopping List", this::showShoppingListView);
        createNavButton("\u2139  About", this::showAboutView);

        VBox navBox = new VBox(4);
        navBox.getChildren().addAll(navButtons);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, navBox, spacer);
        root.setLeft(sidebar);
    }

    private void createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            for (Button b : navButtons) b.getStyleClass().remove("nav-button-active");
            btn.getStyleClass().add("nav-button-active");
            action.run();
        });
        navButtons.add(btn);
    }

    private void navigateTo(int index) {
        navButtons.get(index).fire();
    }

    private String emojiFor(String rawName) {
        String n = (rawName == null) ? "" : rawName.toLowerCase();
        if (n.contains("milk")) return "\uD83E\uDD5B";
        if (n.contains("egg")) return "\uD83E\uDD5A";
        if (n.contains("spinach") || n.contains("lettuce") || n.contains("kale")) return "\uD83E\uDD6C";
        if (n.contains("tomato")) return "\uD83C\uDF45";
        if (n.contains("avocado")) return "\uD83E\uDD51";
        if (n.contains("chicken") || n.contains("turkey")) return "\uD83C\uDF57";
        if (n.contains("cheese") || n.contains("cheddar")) return "\uD83E\uDDC0";
        if (n.contains("bread") || n.contains("toast")) return "\uD83C\uDF5E";
        if (n.contains("onion")) return "\uD83E\uDDC5";
        if (n.contains("carrot")) return "\uD83E\uDD55";
        if (n.contains("pepper")) return "\uD83E\uDED1";
        if (n.contains("spaghetti") || n.contains("pasta")) return "\uD83C\uDF5D";
        if (n.contains("oil")) return "\uD83E\uDED2";
        if (n.contains("yogurt")) return "\uD83E\uDD63";
        if (n.contains("fish") || n.contains("salmon")) return "\uD83D\uDC1F";
        if (n.contains("apple")) return "\uD83C\uDF4E";
        if (n.contains("banana")) return "\uD83C\uDF4C";
        if (n.contains("rice")) return "\uD83C\uDF5A";
        if (n.contains("vitamin") || n.contains("ibuprofen") || n.contains("mg")) return "\uD83D\uDC8A";
        return "\uD83D\uDCE6";
    }

    private Label emojiTile(String name) {
        Label tile = new Label(emojiFor(name));
        tile.getStyleClass().add("emoji-tile");
        return tile;
    }

    private HBox buildTopBar(String titleText, TextField searchField, Button addBtn) {
        Label pageTitle = new Label(titleText);
        pageTitle.getStyleClass().add("page-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(14, pageTitle, spacer);
        if (searchField != null) bar.getChildren().add(searchField);
        if (addBtn != null) bar.getChildren().add(addBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("topbar");
        return bar;
    }

    private ScrollPane wrapPage(HBox topBar, VBox body) {
        body.setPadding(new Insets(22));
        body.getStyleClass().add("content-area");
        VBox whole = new VBox(topBar, body);
        whole.getStyleClass().add("content-area");
        ScrollPane scroll = new ScrollPane(whole);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-area");
        return scroll;
    }

    private void showDashboardView() {
        List<FoodItem> allItems = foodDao.getAll();
        int total = allItems.size();
        int expiringSoon = 0, expired = 0, lowStock = 0;
        List<FoodItem> expiringList = new ArrayList<>();
        for (FoodItem item : allItems) {
            Long days = daysUntilExpiry(item);
            if (days != null && days < 0) expired++;
            if (days != null && days >= 0 && days <= WARNING_DAYS) { expiringSoon++; expiringList.add(item); }
            if (item.getQuantity() <= 1) lowStock++;
        }
        int shoppingCount = shoppingDao.getAll().size();
        List<Recipe> recipes = recipeDao.getAllWithIngredients();
        List<String> invNames = new ArrayList<>();
        for (FoodItem i : allItems) invNames.add(i.getName().toLowerCase());
        int matchCount = 0;
        for (Recipe r : recipes) if (matchPercent(r, invNames) >= 50) matchCount++;

        int attention = expired + expiringSoon;
        Label eyebrow = new Label("Welcome back, Eileen \uD83D\uDC4B");
        eyebrow.getStyleClass().add("banner-eyebrow");
        Label heroTitle = new Label("You have " + attention + " item" + (attention == 1 ? "" : "s") + " that need attention.");
        heroTitle.getStyleClass().add("banner-title");
        Label heroSub = new Label("Use or replace these items soon to reduce waste and save money.");
        heroSub.getStyleClass().add("banner-subtitle");
        VBox banner = new VBox(4, eyebrow, heroTitle, heroSub);
        banner.getStyleClass().add("banner");

        HBox stats = new HBox(14,
            buildStatCard("\uD83D\uDCE6", "icon-purple", total, "Total Items"),
            buildStatCard("\u23F0", "icon-orange", expiringSoon, "Expiring Soon"),
            buildStatCard("\u26A0", "icon-red", expired, "Expired"),
            buildStatCard("\uD83D\uDCC9", "icon-amber", lowStock, "Low Stock"),
            buildStatCard("\uD83D\uDED2", "icon-blue", shoppingCount, "Shopping Needed"),
            buildStatCard("\u2B50", "icon-pink", matchCount, "Recipe Matches"));
        for (javafx.scene.Node n : stats.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        Label expTitle = new Label("\u23F0  Items Expiring Soon");
        expTitle.getStyleClass().add("section-label");
        Button viewAll = new Button("View all \u2192");
        viewAll.getStyleClass().add("btn-soft");
        viewAll.setOnAction(e -> navigateTo(4));
        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        HBox expHeader = new HBox(10, expTitle, s1, viewAll);
        expHeader.setAlignment(Pos.CENTER_LEFT);

        VBox expRows = new VBox(8);
        if (expiringList.isEmpty()) expRows.getChildren().add(new Label("Nothing is expiring soon. Nice."));
        for (FoodItem item : expiringList) expRows.getChildren().add(buildDashExpiringRow(item));

        VBox expCard = new VBox(12, expHeader, expRows);
        expCard.getStyleClass().add("card");
        HBox.setHgrow(expCard, Priority.ALWAYS);

        Label rmTitle = new Label("\u2B50  Top Recipe Matches");
        rmTitle.getStyleClass().add("section-label");
        Button seeAll = new Button("See all \u2192");
        seeAll.getStyleClass().add("btn-soft");
        seeAll.setOnAction(e -> navigateTo(3));
        Region s2 = new Region();
        HBox.setHgrow(s2, Priority.ALWAYS);
        HBox rmHeader = new HBox(10, rmTitle, s2, seeAll);
        rmHeader.setAlignment(Pos.CENTER_LEFT);

        List<Recipe> sorted = new ArrayList<>(recipes);
        sorted.sort(Comparator.comparingInt((Recipe r) -> matchPercent(r, invNames)).reversed());

        VBox rmRows = new VBox(10);
        int shown = 0;
        for (Recipe r : sorted) {
            if (shown >= 2) break;
            rmRows.getChildren().add(buildDashRecipeRow(r, matchPercent(r, invNames)));
            shown++;
        }
        if (shown == 0) rmRows.getChildren().add(new Label("No recipes yet."));

        VBox rmCard = new VBox(12, rmHeader, rmRows);
        rmCard.getStyleClass().add("card");
        rmCard.setPrefWidth(360);
        rmCard.setMinWidth(320);

        HBox columns = new HBox(16, expCard, rmCard);
        VBox body = new VBox(16, banner, stats, columns);

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.getStyleClass().add("search-field");
        Button addBtn = new Button("+ Add Item");
        addBtn.getStyleClass().add("btn-add");
        addBtn.setOnAction(e -> navigateTo(1));

        root.setCenter(wrapPage(buildTopBar("Dashboard", search, addBtn), body));
    }

    private VBox buildStatCard(String emoji, String iconStyle, int number, String labelText) {
        Label icon = new Label(emoji);
        icon.getStyleClass().addAll("stat-icon", iconStyle);
        Label num = new Label(String.valueOf(number));
        num.getStyleClass().add("stat-number");
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("stat-label");
        VBox card = new VBox(6, icon, num, lbl);
        card.getStyleClass().add("stat-card");
        return card;
    }

    private HBox buildDashExpiringRow(FoodItem item) {
        Label name = new Label(item.getName());
        name.getStyleClass().add("item-name");
        Label details = new Label(item.getQuantity() + " " + safeText(item.getUnit()) + " \u00B7 " + safeText(item.getCategory()));
        details.getStyleClass().add("item-details");
        VBox nameBox = new VBox(2, name, details);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label pill = statusPill(item);
        Button findRecipe = new Button("Find Recipe");
        findRecipe.getStyleClass().add("btn-soft");
        findRecipe.setOnAction(e -> navigateTo(3));
        HBox row = new HBox(12, emojiTile(item.getName()), nameBox, pill, findRecipe);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("list-row");
        return row;
    }

    private VBox buildDashRecipeRow(Recipe recipe, int percent) {
        Label name = new Label(recipe.getName());
        name.getStyleClass().add("item-name");
        Label badge = new Label(percent + "% match");
        badge.getStyleClass().add("match-badge");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox top = new HBox(8, name, sp, badge);
        top.setAlignment(Pos.CENTER_LEFT);
        Button cook = new Button("Cook Now");
        cook.getStyleClass().add("btn-gradient");
        cook.setOnAction(e -> showRecipeInstructions(recipe));
        HBox bottom = new HBox(8, new Label(recipe.getServings() + " servings"), new Region(), cook);
        HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);
        bottom.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(8, top, bottom);
        box.getStyleClass().addAll("list-row", "list-row-plain");
        return box;
    }

    private void showInventoryView() {
        ObservableList<FoodItem> items = FXCollections.observableArrayList(foodDao.getAll());
        TableView<FoodItem> table = new TableView<>();
        table.setFixedCellSize(52);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FoodItem, String> nameCol = new TableColumn<>("ITEM");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(col -> new TableCell<FoodItem, String>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setGraphic(null); setText(null); return; }
                Label tile = emojiTile(value);
                Label name = new Label(value);
                name.getStyleClass().add("item-name");
                HBox box = new HBox(10, tile, name);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setText(null);
            }
        });

        TableColumn<FoodItem, String> catCol = new TableColumn<>("CATEGORY");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<FoodItem, Integer> qtyCol = new TableColumn<>("QTY");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<FoodItem, String> unitCol = new TableColumn<>("UNIT");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        TableColumn<FoodItem, String> expCol = new TableColumn<>("EXPIRES");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));

        TableColumn<FoodItem, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(statusTextFor(cell.getValue())));
        statusCol.setCellFactory(col -> new TableCell<FoodItem, String>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                if (empty || value == null) { setGraphic(null); return; }
                FoodItem rowItem = (getTableRow() == null) ? null : getTableRow().getItem();
                Label pill = new Label(value);
                if (rowItem != null) {
                    Long days = daysUntilExpiry(rowItem);
                    if (days == null) pill.getStyleClass().add("pill-nodate");
                    else if (days < 0) pill.getStyleClass().add("pill-expired");
                    else if (days <= WARNING_DAYS) pill.getStyleClass().add("pill-warning");
                    else pill.getStyleClass().add("pill-fresh");
                }
                setGraphic(pill);
            }
        });

        table.getColumns().addAll(nameCol, catCol, qtyCol, unitCol, expCol, statusCol);
        table.setPlaceholder(new Label("No items found. Add your first item to get started."));

        table.setRowFactory(tv -> new TableRow<FoodItem>() {
            @Override protected void updateItem(FoodItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-warning", "row-expired");
                if (item == null || empty) return;
                Long days = daysUntilExpiry(item);
                if (days == null) return;
                if (days < 0) getStyleClass().add("row-expired");
                else if (days <= WARNING_DAYS) getStyleClass().add("row-warning");
            }
        });

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.getStyleClass().add("search-field");

        FilteredList<FoodItem> filtered = new FilteredList<>(items, p -> true);
        final String[] activeCategory = {"All"};
        Runnable applyFilter = () -> {
            String text = search.getText() == null ? "" : search.getText().toLowerCase();
            String cat = activeCategory[0];
            filtered.setPredicate(item -> {
                boolean matchesText = item.getName().toLowerCase().contains(text);
                boolean matchesCat = cat.equals("All") || safeText(item.getCategory()).toLowerCase().contains(cat.toLowerCase());
                return matchesText && matchesCat;
            });
        };
        search.textProperty().addListener((o, a, b) -> applyFilter.run());

        String[] categories = {"All", "Food", "Beverages", "Pantry", "Refrigerated", "Frozen", "Supplements", "Medicine", "Other"};
        HBox tabs = new HBox(4);
        List<Button> tabButtons = new ArrayList<>();
        for (String cat : categories) {
            Button tb = new Button(cat);
            tb.getStyleClass().add("tab-button");
            if (cat.equals("All")) tb.getStyleClass().add("tab-button-active");
            tb.setOnAction(e -> {
                for (Button other : tabButtons) other.getStyleClass().remove("tab-button-active");
                tb.getStyleClass().add("tab-button-active");
                activeCategory[0] = cat;
                applyFilter.run();
            });
            tabButtons.add(tb);
            tabs.getChildren().add(tb);
        }

        SortedList<FoodItem> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.prefHeightProperty().bind(Bindings.size(sorted).multiply(table.getFixedCellSize()).add(48));

        TextField nameField = new TextField(); nameField.setPromptText("Name (e.g. Milk)");
        TextField qtyField = new TextField(); qtyField.setPromptText("Qty"); qtyField.setPrefWidth(70);
        TextField unitField = new TextField(); unitField.setPromptText("Unit"); unitField.setPrefWidth(100);
        TextField catField = new TextField(); catField.setPromptText("Category"); catField.setPrefWidth(120);
        DatePicker expiryPicker = new DatePicker(); expiryPicker.setPromptText("Expiry date");

        Button saveBtn = new Button("Save Item");
        saveBtn.getStyleClass().add("btn-gradient");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-neutral");

        HBox formRow = new HBox(8, nameField, qtyField, unitField, catField, expiryPicker, saveBtn, cancelBtn);
        formRow.setAlignment(Pos.CENTER_LEFT);
        Label formTitle = new Label("Add New Item");
        formTitle.getStyleClass().add("section-label");
        VBox addFormCard = new VBox(10, formTitle, formRow);
        addFormCard.getStyleClass().add("card");
        addFormCard.setVisible(false); addFormCard.setManaged(false);

        Button addBtn = new Button("+ Add Item");
        addBtn.getStyleClass().add("btn-add");
        addBtn.setOnAction(e -> { addFormCard.setVisible(!addFormCard.isVisible()); addFormCard.setManaged(!addFormCard.isManaged()); });
        cancelBtn.setOnAction(e -> { addFormCard.setVisible(false); addFormCard.setManaged(false); });

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String qtyText = qtyField.getText().trim();
            if (name.isEmpty()) { showWarning("Please enter a name for the item."); return; }
            int qty;
            try { qty = Integer.parseInt(qtyText); }
            catch (NumberFormatException ex) { showWarning("Quantity must be a whole number."); return; }
            if (qty <= 0) { showWarning("Quantity must be greater than zero."); return; }
            if (expiryPicker.getValue() == null) { showWarning("Please pick an expiration date."); return; }
            foodDao.insert(new FoodItem(name, qty, unitField.getText().trim(), catField.getText().trim(), expiryPicker.getValue().toString(), null));
            showInventoryView();
        });

        Button editBtn = new Button("Edit Selected");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setOnAction(e -> editItem(table.getSelectionModel().getSelectedItem()));
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setOnAction(e -> deleteItem(table.getSelectionModel().getSelectedItem()));
        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-neutral");
        refreshBtn.setOnAction(e -> showInventoryView());
        HBox actions = new HBox(8, editBtn, deleteBtn, refreshBtn);

        VBox tableCard = new VBox(12, tabs, table, actions);
        tableCard.getStyleClass().add("card");
        VBox body = new VBox(16, tableCard, addFormCard);
        root.setCenter(wrapPage(buildTopBar("Inventory", search, addBtn), body));
    }

    private void editItem(FoodItem selected) {
        if (selected == null) { showWarning("Click a row first."); return; }
        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setTitle("Edit Item"); nameDialog.setHeaderText("Editing: " + selected.getName()); nameDialog.setContentText("Name:");
        String newName = nameDialog.showAndWait().orElse(null);
        if (newName == null) return;
        newName = newName.trim();
        if (newName.isEmpty()) { showWarning("Name can't be blank."); return; }
        TextInputDialog qtyDialog = new TextInputDialog(String.valueOf(selected.getQuantity()));
        qtyDialog.setTitle("Edit Item"); qtyDialog.setHeaderText("Editing: " + newName); qtyDialog.setContentText("Quantity:");
        String qtyText = qtyDialog.showAndWait().orElse(null);
        if (qtyText == null) return;
        int newQty;
        try { newQty = Integer.parseInt(qtyText.trim()); }
        catch (NumberFormatException ex) { showWarning("Quantity must be a whole number."); return; }
        if (newQty <= 0) { showWarning("Quantity must be greater than zero."); return; }
        selected.setName(newName); selected.setQuantity(newQty);
        foodDao.update(selected);
        showInventoryView();
    }

    private void deleteItem(FoodItem selected) {
        if (selected == null) { showWarning("Click a row first."); return; }
        if (confirm("Delete " + selected.getName() + "?")) { foodDao.delete(selected.getId()); showInventoryView(); }
    }

    private void showExpirationView() {
        Label bTitle = new Label("Save money by using what you already own.");
        bTitle.getStyleClass().add("banner-title"); bTitle.setStyle("-fx-font-size: 16px;");
        Label bSub = new Label("Plan meals around expiring items to reduce food waste and unnecessary spending.");
        bSub.getStyleClass().add("banner-subtitle");
        VBox banner = new VBox(4, bTitle, bSub);
        banner.getStyleClass().add("banner");

        List<FoodItem> allItems = foodDao.getAll();
        List<FoodItem> expired = new ArrayList<>(), warning = new ArrayList<>(), fresh = new ArrayList<>();
        for (FoodItem item : allItems) {
            Long days = daysUntilExpiry(item);
            if (days == null) fresh.add(item);
            else if (days < 0) expired.add(item);
            else if (days <= WARNING_DAYS) warning.add(item);
            else fresh.add(item);
        }

        VBox sections = new VBox(22);
        if (!expired.isEmpty()) sections.getChildren().add(buildExpirationSection("Expired", "Remove or replace these items immediately.", expired, "list-row-expired"));
        if (!warning.isEmpty()) sections.getChildren().add(buildExpirationSection("Expiring Within " + WARNING_DAYS + " Days", "Plan meals around these before they expire.", warning, "list-row-warning"));
        if (!fresh.isEmpty()) sections.getChildren().add(buildExpirationSection("Fresh", "These items are in good shape.", fresh, "list-row-plain"));
        if (allItems.isEmpty()) sections.getChildren().add(new Label("No items in your inventory yet."));

        VBox body = new VBox(16, banner, sections);
        root.setCenter(wrapPage(buildTopBar("Expiration Center", null, null), body));
    }

    private VBox buildExpirationSection(String title, String note, List<FoodItem> sectionItems, String rowStyle) {
        Label label = new Label(title + "   " + sectionItems.size());
        label.getStyleClass().add("section-label");
        Label noteLabel = new Label(note);
        noteLabel.getStyleClass().add("section-note");
        VBox rows = new VBox(8);
        for (FoodItem item : sectionItems) rows.getChildren().add(buildExpirationRow(item, rowStyle));
        return new VBox(6, label, noteLabel, rows);
    }

    private HBox buildExpirationRow(FoodItem item, String rowStyle) {
        Label name = new Label(item.getName());
        name.getStyleClass().add("item-name");
        Label details = new Label(item.getQuantity() + " " + safeText(item.getUnit()) + " \u00B7 " + safeText(item.getCategory())
                + "\nExp: " + (item.getExpirationDate() == null ? "\u2014" : item.getExpirationDate()));
        details.getStyleClass().add("item-details");
        VBox nameBox = new VBox(2, name, details);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label pill = statusPill(item);
        Button useBtn = new Button("Use");
        useBtn.getStyleClass().add("btn-gradient");
        useBtn.setOnAction(e -> useItem(item));
        Button recipesBtn = new Button("Recipes");
        recipesBtn.getStyleClass().add("btn-soft");
        recipesBtn.setOnAction(e -> navigateTo(3));
        HBox row = new HBox(14, emojiTile(item.getName()), nameBox, pill, useBtn, recipesBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("list-row", rowStyle);
        return row;
    }

    private void useItem(FoodItem item) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Use " + item.getName());
        dialog.setHeaderText(item.getName() + " \u2014 Available: " + item.getQuantity() + " " + safeText(item.getUnit()));
        dialog.setContentText("Amount to use:");
        String amountText = dialog.showAndWait().orElse(null);
        if (amountText == null) return;
        int amount;
        try { amount = Integer.parseInt(amountText.trim()); }
        catch (NumberFormatException ex) { showWarning("Please enter a whole number."); return; }
        if (amount <= 0) { showWarning("Amount must be greater than zero."); return; }
        int remaining = item.getQuantity() - amount;
        if (remaining <= 0) {
            if (confirm(item.getName() + " will be used up. Add it to your shopping list?")) shoppingDao.insert(new ShoppingItem(item.getName(), 1));
            foodDao.delete(item.getId());
        } else {
            item.setQuantity(remaining);
            foodDao.update(item);
        }
        showExpirationView();
    }

    private void showRecipesView(boolean sortedByMatch) {
        Label bTitle = new Label("Turn what you already have into your next meal.");
        bTitle.getStyleClass().add("banner-title"); bTitle.setStyle("-fx-font-size: 16px;");
        Label bSub = new Label("Recipes are matched against your current inventory.");
        bSub.getStyleClass().add("banner-subtitle");
        VBox banner = new VBox(4, bTitle, bSub);
        banner.getStyleClass().add("banner");

        List<FoodItem> inventory = foodDao.getAll();
        List<String> invNames = new ArrayList<>();
        for (FoodItem item : inventory) invNames.add(item.getName().toLowerCase());
        List<Recipe> recipes = recipeDao.getAllWithIngredients();
        if (sortedByMatch) recipes.sort(Comparator.comparingInt((Recipe r) -> matchPercent(r, invNames)).reversed());

        VBox cards = new VBox(14);
        if (recipes.isEmpty()) cards.getChildren().add(new Label("No recipes yet."));
        for (Recipe r : recipes) cards.getChildren().add(buildRecipeCard(r, invNames));

        VBox body = new VBox(16, banner, cards);
        root.setCenter(wrapPage(buildTopBar(sortedByMatch ? "Recipe Matches" : "Recipes", null, null), body));
    }

    private int matchPercent(Recipe recipe, List<String> invNames) {
        int total = recipe.getIngredients().size();
        if (total == 0) return 0;
        int available = 0;
        for (String ing : recipe.getIngredients()) if (ingredientInInventory(ing, invNames)) available++;
        return (int) Math.round(available * 100.0 / total);
    }

    private boolean ingredientInInventory(String ingredient, List<String> invNames) {
        String ingLower = ingredient.toLowerCase();
        for (String inv : invNames) if (inv.contains(ingLower) || ingLower.contains(inv)) return true;
        return false;
    }

    private VBox buildRecipeCard(Recipe recipe, List<String> invNames) {
        List<String> available = new ArrayList<>(), missing = new ArrayList<>();
        for (String ing : recipe.getIngredients()) {
            if (ingredientInInventory(ing, invNames)) available.add(ing); else missing.add(ing);
        }
        int percent = matchPercent(recipe, invNames);

        Label name = new Label(recipe.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #12061F;");
        Label badge = new Label(percent + "% match");
        badge.getStyleClass().add("match-badge");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox header = new HBox(10, name, sp, badge);
        header.setAlignment(Pos.CENTER_LEFT);
        Label meta = new Label(recipe.getServings() + " servings");
        meta.getStyleClass().add("info-label");

        Label availHeader = new Label("AVAILABLE IN INVENTORY");
        availHeader.getStyleClass().add("tag-header");
        VBox availBox = new VBox(3, availHeader);
        for (String ing : available) { Label l = new Label("\u2713 " + ing); l.getStyleClass().add("tag-available"); availBox.getChildren().add(l); }

        Label missHeader = new Label("MISSING INGREDIENTS");
        missHeader.getStyleClass().add("tag-header");
        VBox missBox = new VBox(3, missHeader);
        if (missing.isEmpty()) { Label none = new Label("None \u2014 cook it today!"); none.getStyleClass().add("tag-available"); missBox.getChildren().add(none); }
        for (String ing : missing) { Label l = new Label("\u2717 " + ing); l.getStyleClass().add("tag-missing"); missBox.getChildren().add(l); }

        HBox ingRow = new HBox(40, availBox, missBox);
        Button view = new Button("View Recipe");
        view.getStyleClass().add("btn-gradient");
        view.setOnAction(e -> showRecipeInstructions(recipe));
        Button addMissing = new Button("Add Missing to Shopping List");
        addMissing.getStyleClass().add("btn-soft");
        addMissing.setDisable(missing.isEmpty());
        addMissing.setOnAction(e -> {
            for (String ing : missing) shoppingDao.insert(new ShoppingItem(ing, 1));
            showInfo("Added " + missing.size() + " item(s) to your shopping list.");
        });
        HBox buttons = new HBox(8, view, addMissing);

        VBox card = new VBox(10, header, meta, ingRow, buttons);
        card.getStyleClass().add("card");
        return card;
    }

    private void showRecipeInstructions(Recipe recipe) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(recipe.getName());
        alert.setHeaderText(recipe.getName() + "  \u00B7  " + recipe.getServings() + " servings");
        alert.setContentText(recipe.getInstructions());
        alert.showAndWait();
    }

    private void showShoppingListView() {
        List<ShoppingItem> shoppingItems = shoppingDao.getAll();
        VBox rows = new VBox(8);
        if (shoppingItems.isEmpty()) rows.getChildren().add(new Label("Your shopping list is empty."));
        for (ShoppingItem item : shoppingItems) rows.getChildren().add(buildShoppingRow(item));

        TextField nameField = new TextField(); nameField.setPromptText("Item name");
        TextField qtyField = new TextField(); qtyField.setPromptText("Qty"); qtyField.setPrefWidth(70);
        Button addBtn = new Button("+ Add to List");
        addBtn.getStyleClass().add("btn-add");
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showWarning("Please enter an item name."); return; }
            int qty = 1;
            String qtyText = qtyField.getText().trim();
            if (!qtyText.isEmpty()) {
                try { qty = Integer.parseInt(qtyText); }
                catch (NumberFormatException ex) { showWarning("Quantity must be a whole number."); return; }
            }
            shoppingDao.insert(new ShoppingItem(name, qty));
            showShoppingListView();
        });
        HBox addRow = new HBox(8, nameField, qtyField, addBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);
        VBox card = new VBox(12, rows, addRow);
        card.getStyleClass().add("card");
        VBox body = new VBox(16, card);
        root.setCenter(wrapPage(buildTopBar("Shopping List", null, null), body));
    }

    private HBox buildShoppingRow(ShoppingItem item) {
        Label name = new Label(item.getItemName());
        name.getStyleClass().add("item-name");
        Label qty = new Label("Qty: " + item.getQuantity());
        qty.getStyleClass().add("item-details");
        VBox nameBox = new VBox(2, name, qty);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Button remove = new Button("Remove");
        remove.getStyleClass().add("btn-delete");
        remove.setOnAction(e -> { shoppingDao.delete(item.getId()); showShoppingListView(); });
        HBox row = new HBox(14, emojiTile(item.getItemName()), nameBox, remove);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("list-row", "list-row-plain");
        return row;
    }

    private void showAboutView() {
        Label heroTitle = new Label("ConsumeWise");
        heroTitle.getStyleClass().add("banner-title");
        Label heroSub = new Label("Manage what you have. Use it before you lose it.");
        heroSub.getStyleClass().add("banner-subtitle");
        VBox hero = new VBox(6, heroTitle, heroSub);
        hero.getStyleClass().add("banner");

        GridPane grid = new GridPane();
        grid.setVgap(10); grid.setHgap(24);
        addInfoRow(grid, 0, "Course", "COP 2800 \u2014 Intro to Java");
        addInfoRow(grid, 1, "Student", "Eileen");
        addInfoRow(grid, 2, "Language", "Java 21");
        addInfoRow(grid, 3, "GUI", "JavaFX + JavaFX CSS");
        addInfoRow(grid, 4, "Database", "SQLite 3 (JDBC)");
        addInfoRow(grid, 5, "Editor", "Visual Studio Code");

        Label infoTitle = new Label("Project Info");
        infoTitle.getStyleClass().add("section-label");
        VBox infoCard = new VBox(12, infoTitle, grid);
        infoCard.getStyleClass().add("card");

        String[] reqs = {"JavaFX GUI", "SQLite Database", "Full CRUD", "Input Validation", "Exception Handling", "OOP Design", "Recipe Matching", "Shopping List"};
        FlowPane reqPane = new FlowPane(10, 10);
        for (String r : reqs) { Label chip = new Label("\u2713 " + r); chip.getStyleClass().addAll("legend-chip", "chip-fresh"); reqPane.getChildren().add(chip); }
        Label reqTitle = new Label("Assignment Requirements Met");
        reqTitle.getStyleClass().add("section-label");
        VBox reqCard = new VBox(12, reqTitle, reqPane);
        reqCard.getStyleClass().add("card");

        Label futureTitle = new Label("Future Features");
        futureTitle.getStyleClass().add("section-label");
        Label futureNote = new Label("PIN login, barcode scanning, cloud sync, and a mobile companion app are planned as future improvements.");
        futureNote.setWrapText(true);
        futureNote.getStyleClass().add("info-label");
        VBox futureCard = new VBox(12, futureTitle, futureNote);
        futureCard.getStyleClass().add("card");

        VBox body = new VBox(16, hero, infoCard, reqCard, futureCard);
        root.setCenter(wrapPage(buildTopBar("About ConsumeWise", null, null), body));
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label); l.getStyleClass().add("info-label");
        Label v = new Label(value); v.getStyleClass().add("info-value");
        grid.add(l, 0, row); grid.add(v, 1, row);
    }

    private Label statusPill(FoodItem item) {
        Label pill = new Label(statusTextFor(item));
        Long days = daysUntilExpiry(item);
        if (days == null) pill.getStyleClass().add("pill-nodate");
        else if (days < 0) pill.getStyleClass().add("pill-expired");
        else if (days <= WARNING_DAYS) pill.getStyleClass().add("pill-warning");
        else pill.getStyleClass().add("pill-fresh");
        return pill;
    }

    private Long daysUntilExpiry(FoodItem item) {
        String dateText = item.getExpirationDate();
        if (dateText == null || dateText.isBlank()) return null;
        try { LocalDate expiry = LocalDate.parse(dateText); return ChronoUnit.DAYS.between(LocalDate.now(), expiry); }
        catch (Exception ex) { return null; }
    }

    private String statusTextFor(FoodItem item) {
        Long days = daysUntilExpiry(item);
        if (days == null) return "No Date";
        if (days < 0) return "EXPIRED";
        if (days == 0) return "Expires today";
        if (days == 1) return "1 day left";
        if (days <= WARNING_DAYS) return days + " days left";
        return days + " days";
    }

    private String safeText(String value) { return (value == null) ? "" : value; }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null); alert.setTitle("Please confirm");
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null); alert.setTitle("Hold on"); alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null); alert.setTitle("Done"); alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}