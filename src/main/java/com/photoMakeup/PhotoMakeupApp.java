package com.photoMakeup;

import com.photoMakeup.service.ImageService;
import com.photoMakeup.ui.CanvasPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PhotoMakeupApp extends Application {

    private CanvasPanel canvasPanel;
    private ImageService imageService;
    private Label statusLabel;
    private BorderPane root;
    private VBox rightPanel;

    private static final double RIGHT_PANEL_WIDTH = 280;

    @Override
    public void start(Stage stage) throws Exception {
        imageService = new ImageService();
        canvasPanel = new CanvasPanel();

        root = new BorderPane();
        root.setStyle("-fx-padding: 0;");

        HBox toolbar = createToolbar();
        root.setTop(toolbar);

        root.setCenter(canvasPanel);

        rightPanel = createRightPanel();
        root.setRight(rightPanel);

        statusLabel = new Label("Готово");
        HBox statusBar = new HBox();
        statusLabel.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-width: 1 0 0 0");
        statusBar.getChildren().add(statusLabel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("PhotoMakeup - Разметка фотографий");

        try {
            // Вариант 1: Если иконка в resources
            Image icon = new Image(getClass().getResourceAsStream("/marker-128.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            // Если файл не найден, просто пропускаем
            System.out.println("Иконка не найдена: " + e.getMessage());
        }

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            onWindowResized();
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            onWindowResized();
        });

        stage.setScene(scene);
        stage.show();

        stage.setOnShown(event -> {
            enforceRightPanelWidth();
        });
    }

    private void onWindowResized() {
        enforceRightPanelWidth();
    }

    private void enforceRightPanelWidth() {
        if (rightPanel != null) {
            rightPanel.setPrefWidth(RIGHT_PANEL_WIDTH);
            rightPanel.setMinWidth(RIGHT_PANEL_WIDTH);
            rightPanel.setMaxWidth(RIGHT_PANEL_WIDTH);

            rightPanel.layout();
            root.layout();
        }
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));

        toolbar.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Button openButton = new Button("\uD83D\uDCC1 Открыть фото");
        openButton.setOnAction(e -> handlerOpenImage());

        Button clearButton = new Button("❌ Очистить разметку");
        clearButton.setOnAction(e -> canvasPanel.clearMarks());

        Button saveButton = new Button("💾 Сохранить JSON");
        saveButton.setOnAction(e -> handleSaveMakeup());

        Button loadButton = new Button("📂 Загрузить JSON");
        loadButton.setOnAction(e -> handlerLoadMakeup());

        toolbar.getChildren().addAll(openButton, clearButton, saveButton, loadButton);

        return toolbar;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1");

        panel.setPrefWidth(RIGHT_PANEL_WIDTH);
        panel.setMinWidth(RIGHT_PANEL_WIDTH);
        panel.setMaxWidth(RIGHT_PANEL_WIDTH);

        // =================== coordinates block =========================================
        Label cursorLabel = new Label("Координаты курсора:");
        cursorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        VBox coordBox = new VBox(5);
        coordBox.setStyle("-fx-font-size: 11; -fx-border-width: 1; -fx-padding: 10;");

        Label coordXLabel = new Label("X: —");
        coordXLabel.setStyle("-fx-font-size: 11; -fx-font-family: monospace;");

        Label coordYLabel = new Label("Y: —");
        coordYLabel.setStyle("-fx-font-size: 11; -fx-font-family: monospace;");

        Label screenCoordLabel = new Label("Экран (px): —");
        screenCoordLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #888; -fx-font-family: monospace;");

        coordBox.getChildren().addAll(coordXLabel, coordYLabel, new Separator(), screenCoordLabel);

        canvasPanel.mouseCoordProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double[] coords = newVal;
                // coords[0] = X в координатах изображения
                // coords[1] = Y в координатах изображения
                // coords[2] = screen X
                // coords[3] = screen Y

                coordXLabel.setText(String.format("X: %.1f px", coords[0]));
                coordYLabel.setText(String.format("Y: %.1f px", coords[1]));
                screenCoordLabel.setText(String.format("Экран: (%.0f, %.0f)", coords[2], coords[3]));
            } else {
                coordXLabel.setText("X: —");
                coordYLabel.setText("Y: —");
                screenCoordLabel.setText("Экран (px): —");
            }
        });

        // =================== Statistics block ==========================================
        Label statsLabel = new Label("Статистика: ");
        Label rectangleCountLabel = new Label("Прямоугольников: 0");

        canvasPanel.getMarksCount().addListener((observable, oldValue, newValue) -> {
            rectangleCountLabel.setText("Прямоугольников: " + newValue);
        });

        Label instructionLabel = new Label(
                "Инструкция:\n" +
                        "• Левая кнопка мыши: рисовать\n" +
                        "• Правая кнопка: удалить\n" +
                        "• колесо: зум\n" +
                        "• Средняя книпка мыши: панорама"
        );

        instructionLabel.setWrapText(true);
        instructionLabel.setStyle("-fx-font-size: 11;");

        VBox.setVgrow(instructionLabel, Priority.ALWAYS);

        panel.getChildren().addAll(
                cursorLabel, coordBox,
                new Separator(),
                statsLabel, rectangleCountLabel,
                new Separator(),
                instructionLabel);

        return panel;
    }

    private void handlerOpenImage() {
        imageService.openImageDialog(canvasPanel);
        statusLabel.setText("Изображение загружено");
    }

    private void handleSaveMakeup() {
        imageService.saveMarksDialog(canvasPanel);
        statusLabel.setText("Разметка сохранена в JSON");
    }

    private void handlerLoadMakeup() {
        imageService.loadMarksDialog(canvasPanel);
        statusLabel.setText("Разметка загружена из JSON");
    }

    public static void main(String[] args) {
        launch();
    }
}
