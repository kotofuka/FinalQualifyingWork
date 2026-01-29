package com.photoMakeup;

import com.photoMakeup.service.FileInteractionService;
import com.photoMakeup.ui.CanvasPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class PhotoMakeupApp extends Application {
    private BorderPane root;
    private Scene scene;

    // Service экземпляры
    private FileInteractionService fileInteractionService;

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
        fileInteractionService = new FileInteractionService();

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
        openFileItem.setOnAction(e -> fileInteractionService.openLoadImageDialog(canvasPanel));
        savePhotoItem.setOnAction(e -> fileInteractionService.openSaveImageDialog(canvasPanel));
        loadMarksItem.setOnAction(e -> fileInteractionService.openLoadMarksDialog(canvasPanel));
        saveMarksItem.setOnAction(e -> fileInteractionService.openSaveMarksDialog(canvasPanel));

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

        clearMarksItem.setOnAction(e -> canvasPanel.clearMarks());
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
        fileInteractionService.setFileNameLabel(fileNameLabel);
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
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1;");

        // Координаты курсора
        Label cursorLabel = new Label("📍 Координаты курсора:");
        cursorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        VBox coordBox = new VBox(5);
        coordBox.setStyle("-fx-font-size: 11; -fx-border-width: 1; -fx-padding: 10; -fx-border-color: #d0d0d0;");

        Label coordXLabel = new Label("X: —");
        coordXLabel.setStyle("-fx-font-size: 11; -fx-font-family: monospace;");
        Label coordYLabel = new Label("Y: —");
        coordYLabel.setStyle("-fx-font-size: 11; -fx-font-family: monospace;");
        Label screenCoordLabel = new Label("Экран (px): —");
        screenCoordLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #888; -fx-font-family: monospace;");

        coordBox.getChildren().addAll(coordXLabel, coordYLabel, new Separator(), screenCoordLabel);

        // слушатель на изменение позиции мышки
        canvasPanel.setOnMouseMoved(e -> {
            var viewModel = canvasPanel.getViewModel();
            double imageX = (e.getX() - viewModel.getPanX()) / viewModel.getZoom();
            double imageY = (e.getY() - viewModel.getPanY()) / viewModel.getZoom();
            coordXLabel.setText(String.format("X: %.1f px", imageX));
            coordYLabel.setText(String.format("Y: %.1f px", imageY));
            screenCoordLabel.setText(String.format("Экран: (%.0f, %.0f)", e.getX(), e.getY()));
        });

        Label rectangleCountLabel = new Label("Прямоугольников: 0");
        rectangleCountLabel.setStyle("-fx-font-size: 11;");

        // Обновление счётчика разметок
        canvasPanel.getViewModel().marksCountProperty().addListener((observable, oldValue, newValue) -> {
            rectangleCountLabel.setText("Прямоугольников: " + newValue);
        });

        panel.getChildren().addAll(cursorLabel, coordBox, new Separator(), rectangleCountLabel);

        return panel;
    }

    private VBox createNormalizePanel() {
        return new VBox();
    }

    private VBox createBinMethodsPanel() {
        VBox binMethodsPanel = new VBox();

        return binMethodsPanel;
    }
}
