package com.photoMakeup;

import com.photoMakeup.ui.CanvasPanel;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class PhotoMakeupApp extends Application {
    private BorderPane root;
    private Scene scene;

    // UI компоненты
    private Label fileNameLabel;

    private CanvasPanel canvasPanel;

    private TabPane rightTabPane;
    private VBox statisticsPanel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        root = new BorderPane();
        root.setStyle("-fx-padding: 0;");

        canvasPanel = new CanvasPanel();

        // services initialization

        // additional component initialization
        root.setTop(createToolbar());
        root.setBottom(createStatusBar());
        root.setRight(createRightTabs());
        root.setCenter(canvasPanel);

        // root component initialization and start app

        scene = new Scene(root, 1600, 900);
        stage.setTitle("PhotoMakeup - Разметка и нормализация фотографий");

        // set app icon
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/marker-128.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Если файл не найден, просто пропускаем
            System.out.println("Иконка не найдена: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // выпадающий список Файл
        MenuButton fileMenuButton = new MenuButton("Файл");

        MenuItem openFileItem = new MenuItem("Открыть файл");
        MenuItem savePhotoItem = new MenuItem("Сохранить фото");

        MenuItem loadMarksItem = new MenuItem("Загрузить разметку");
        MenuItem saveMarksItem = new MenuItem("Сохранить разметку");

        fileMenuButton.getItems().addAll(openFileItem, savePhotoItem, loadMarksItem, saveMarksItem);
        // обработка действий для кнопок в меню "Файл"
        openFileItem.setOnAction(e -> System.out.println("Открыть файл"));
        savePhotoItem.setOnAction(e -> System.out.println("Сохранить фотографию"));
        loadMarksItem.setOnAction(e -> System.out.println("Загрузить разметку"));
        saveMarksItem.setOnAction(e -> System.out.println("Сохранить разметку"));

        // кнопка Redo
        Button redoButton = new Button("Отменить");
        redoButton.setOnAction(e -> System.out.println("Отменить"));
//        KeyCombination redoShortcut = KeyCombination.keyCombination("Ctrl+Z");
//        redoButton.setTooltip(new Tooltip("Горячая клавиша: " + redoShortcut.getDisplayText()));

        // выпадающий список Инструменты
        MenuButton toolMenuButton = new MenuButton("Инструменты");

        MenuItem clearMarksItem = new MenuItem("Очистить разметку");

        MenuItem findCornersItem = new MenuItem("Обраружить углы");
        MenuItem normalizeItem = new MenuItem("Нормализовать");

        clearMarksItem.setOnAction(e -> System.out.println("Очистить разметку"));
        findCornersItem.setOnAction(e -> System.out.println("Обраружить углы"));
        normalizeItem.setOnAction(e -> System.out.println("Нормализовать"));

        toolMenuButton.getItems().addAll(clearMarksItem, findCornersItem, normalizeItem);

        toolbar.getChildren().addAll(fileMenuButton,
                                    new Separator(),
                                    redoButton,
                                    new Separator(),
                                    toolMenuButton);

        return toolbar;
    }

    // StatusBar at the bottom of the window
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        fileNameLabel = new Label("Нет открытого файла");
        fileNameLabel.setPadding(new Insets(5));
        fileNameLabel.setStyle("-fx-font-size: 11;");
        statusBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        statusBar.getChildren().add(fileNameLabel);
        return statusBar;
    }

    private TabPane createRightTabs(){
        rightTabPane = new TabPane();
        rightTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Раздел со статистикой
        statisticsPanel = createStatisticsPanel();
        Tab statisticTab = new Tab("Статистика", statisticsPanel);
        statisticTab.setClosable(false);

        // Раздел с действиями по номализации
        VBox normalizePanel = createNormalizePanel();
        Tab normalizeTab = new Tab("Нормализация", normalizePanel);
        normalizeTab.setClosable(false);

        // Раздел с методами бинаризации
        VBox binMethodsPanel = createBinMethodsPanel();
        Tab binMethodsTab = new Tab("Методы биноризации", binMethodsPanel);
        binMethodsTab.setClosable(false);

        rightTabPane.getTabs().addAll(statisticTab, normalizeTab, binMethodsTab);
        return rightTabPane;
    }

    private VBox createStatisticsPanel() {
        return new VBox();
    }

    private VBox createNormalizePanel() {
        return new VBox();
    }

    private VBox createBinMethodsPanel() {
        VBox binMethodsPanel = new VBox();

        return binMethodsPanel;
    }
}
