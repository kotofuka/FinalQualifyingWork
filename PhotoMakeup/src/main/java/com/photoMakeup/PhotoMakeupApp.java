package com.photoMakeup;

import com.photoMakeup.model.CornerPoint;
import com.photoMakeup.service.DocumentDetectionService;
import com.photoMakeup.service.ImageService;
import com.photoMakeup.service.PerspectiveTransformService;
import com.photoMakeup.ui.CanvasPanel;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PhotoMakeupApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(PhotoMakeupApp.class);
    // === UI компоненты ===
    private CanvasPanel canvasPanel;
    private Label statusLabel;
    private BorderPane root;
    private VBox rightPanel;
    private TabPane tabPane;
    private ImageView resultImageView;
    private Label cornersCountLabel;
    private ProgressIndicator progressIndicator;

    // === Сервисы ===
    private ImageService imageService;                           // Загрузка/сохранение файлов
    private DocumentDetectionService detectionService;           // Поиск углов
    private PerspectiveTransformService transformService;        // Преобразование

    // ===== ДАННЫЕ =====
    private Mat currentMatImage;           // Текущее изображение в формате OpenCV
    private Mat normalizedImage;           // Результат нормализации
    private List<CornerPoint> detectedCorners;  // Обнаруженные углы
    private static final double RIGHT_PANEL_WIDTH = 280;

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("========== ЗАПУСК ПРИЛОЖЕНИЯ PhotoMakeup ==========");

        imageService = new ImageService();
        detectionService = new DocumentDetectionService();
        transformService = new PerspectiveTransformService();
        canvasPanel = new CanvasPanel();

        root = new BorderPane();
        root.setStyle("-fx-padding: 0;");

        root.setTop(createToolbar());
        root.setCenter(createTabs());
        root.setRight(createRightPanel());
        root.setBottom(createStatusBar());

        Scene scene = new Scene(root, 1600, 900);
        stage.setTitle("PhotoMakeup - Разметка и нормализация фотографий");
        stage.setScene(scene);
        stage.show();

        logger.info("Приложение успешно запущено");
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Button openButton = new Button("📁 Открыть фото");
        openButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        openButton.setOnAction(e -> handlerOpenImage());

        Button clearButton = new Button("❌ Очистить разметку");
        clearButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        clearButton.setOnAction(e -> {
            canvasPanel.clearMarks();
            statusLabel.setText("Разметка очищена");
        });

        // ===== БЛОК 2: Сохранение/загрузка разметки =====
        Button saveButton = new Button("💾 Сохранить JSON");
        saveButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        saveButton.setOnAction(e -> handleSaveMakeup());

        Button loadButton = new Button("📂 Загрузить JSON");
        loadButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        loadButton.setOnAction(e -> handlerLoadMakeup());

        // ===== БЛОК 3: Нормализация =====
        Button detectButton = new Button("🎯 Обнаружить углы");
        detectButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        detectButton.setOnAction(e -> handleDetectCorners());

        Button normalizeButton = new Button("✨ Нормализовать");
        normalizeButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        normalizeButton.setOnAction(e -> handleNormalize());

        toolbar.getChildren().addAll(
                openButton, clearButton,
                new Separator(),
                saveButton, loadButton,
                new Separator(),
                detectButton, normalizeButton
        );

        return toolbar;
    }

    private TabPane createTabs() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // ===== TAB 1: Разметка =====
        Tab markupTab = new Tab("📝 Разметка", canvasPanel);
        markupTab.setClosable(false);

        // ===== TAB 2: Нормализация =====
        VBox normalizationPanel = createNormalizationPanel();
        Tab normalizeTab = new Tab("✨ Нормализация", normalizationPanel);
        normalizeTab.setClosable(false);

        tabPane.getTabs().addAll(markupTab, normalizeTab);
        return tabPane;
    }

    private VBox createNormalizationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        resultImageView = new ImageView();
        resultImageView.setFitWidth(900);
        resultImageView.setFitHeight(600);
        resultImageView.setPreserveRatio(true);

        ScrollPane scrollPane = new ScrollPane(resultImageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Button saveResultButton = new Button("💾 Сохранить результат");
        saveResultButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        saveResultButton.setOnAction(e -> handleSaveNormalized());

        HBox btnBox = new HBox(10);
        btnBox.getChildren().addAll(saveResultButton);
        btnBox.setPadding(new Insets(10));

        panel.getChildren().addAll(btnBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1;");
        panel.setPrefWidth(RIGHT_PANEL_WIDTH);
        panel.setMinWidth(RIGHT_PANEL_WIDTH);
        panel.setMaxWidth(RIGHT_PANEL_WIDTH);

        // ===== РАЗДЕЛ: Координаты курсора =====
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

        // ===== СЛУШАТЕЛЬ: Обновление координат при движении мыши =====
        // (требует добавить mouseCoordProperty() в CanvasPanel)
        canvasPanel.setOnMouseMoved(e -> {
            double imageX = (e.getX() - canvasPanel.getPanX()) / canvasPanel.getZoom();
            double imageY = (e.getY() - canvasPanel.getPanY()) / canvasPanel.getZoom();
            coordXLabel.setText(String.format("X: %.1f px", imageX));
            coordYLabel.setText(String.format("Y: %.1f px", imageY));
            screenCoordLabel.setText(String.format("Экран: (%.0f, %.0f)", e.getX(), e.getY()));
        });

        // ===== РАЗДЕЛ: Статистика =====
        Label statsLabel = new Label("📊 Статистика:");
        statsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        Label rectangleCountLabel = new Label("Прямоугольников: 0");
        rectangleCountLabel.setStyle("-fx-font-size: 11;");

        cornersCountLabel = new Label("Углов обнаружено: 0");
        cornersCountLabel.setStyle("-fx-font-size: 11;");

        // Обновление счётчика разметок
        canvasPanel.getMarksCount().addListener((observable, oldValue, newValue) -> {
            rectangleCountLabel.setText("Прямоугольников: " + newValue);
        });

        // ===== РАЗДЕЛ: Прогресс =====
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        progressIndicator.setVisible(false);  // Скрываем по умолчанию

        // ===== РАЗДЕЛ: Инструкции =====
        Label instructionLabel = new Label(
                "⌨️  ИНСТРУКЦИИ:\n" +
                        "\n" +
                        "🖱️  ЛКМ: Рисовать\n" +
                        "   прямоугольники\n" +
                        "\n" +
                        "🔴 ПКМ: Удалить\n" +
                        "   разметку\n" +
                        "\n" +
                        "🔄 Колесо мыши: Зум\n" +
                        "\n" +
                        "🖱️  Средняя кнопка:\n" +
                        "   Панорама\n" +
                        "\n" +
                        "💡 СОВЕТЫ:\n" +
                        "1. Обнаружьте углы\n" +
                        "2. Нормализуйте фото\n" +
                        "3. Сохраните результат"
        );
        instructionLabel.setWrapText(true);
        instructionLabel.setStyle("-fx-font-size: 10; -fx-padding: 10;");

        VBox.setVgrow(instructionLabel, Priority.ALWAYS);

        panel.getChildren().addAll(
                cursorLabel, coordBox,
                new Separator(),
                statsLabel, rectangleCountLabel, cornersCountLabel,
                progressIndicator,
                new Separator(),
                instructionLabel
        );

        return panel;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusLabel = new Label("✅ Готово");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-font-size: 11;");
        statusBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
        statusBar.getChildren().add(statusLabel);
        return statusBar;
    }

    private void handlerOpenImage() {
        logger.info(">>> Нажата кнопка 'Открыть фото'");

        try {
            imageService.openImageDialog(canvasPanel);
            statusLabel.setText("✅ Изображение загружено");

            // ✅ ИСПРАВЛЕНО: Конвертируем Image → Mat безопасно
            Image fxImage = canvasPanel.getCurrentImage();  // Нужно добавить этот getter в CanvasPanel!

            if (fxImage != null) {
                currentMatImage = convertImageToMat(fxImage);

                if (currentMatImage != null && !currentMatImage.empty()) {
                    logger.info("✅ Mat изображение готово для обработки (размер: {} x {})",
                            currentMatImage.width(), currentMatImage.height());
                    statusLabel.setText("✅ Изображение готово к обработке");
                } else {
                    logger.warn("⚠️  Mat изображение пусто или null");
                    statusLabel.setText("⚠️  Ошибка при обработке изображения");
                }
            } else {
                logger.warn("⚠️  FXImage не загружена");
            }

            // Переходим на первую вкладку (Разметка)
            tabPane.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error("❌ Ошибка при открытии изображения", e);
            statusLabel.setText("❌ Ошибка: " + e.getMessage());
        }
    }

    private void handleSaveMakeup() {
        logger.info(">>> Нажата кнопка 'Сохранить JSON'");

        if (canvasPanel.getRectangles().isEmpty()) {
            statusLabel.setText("⚠️  Нет разметок для сохранения!");
            return;
        }

        imageService.saveMarksDialog(canvasPanel);
        statusLabel.setText("✅ Разметка сохранена в JSON");
    }

    private void handlerLoadMakeup() {
        logger.info(">>> Нажата кнопка 'Загрузить JSON'");

        imageService.loadMarksDialog(canvasPanel);
        statusLabel.setText("✅ Разметка загружена из JSON");
    }

    private void handleDetectCorners() {
        logger.info(">>> Нажата кнопка 'Обнаружить углы'");

        // Проверяем, загружено ли изображение
        if (currentMatImage == null) {
            statusLabel.setText("❌ Сначала загрузите изображение!");
            logger.warn("Попытка обнаружить углы без загруженного изображения");
            return;
        }

        // Показываем индикатор загрузки
        progressIndicator.setVisible(true);
        statusLabel.setText("⏳ Обнаруживаю углы документа...");
        logger.info("Начало обнаружения углов");

        // Запускаем в отдельном потоке (чтобы UI не зависал)
        CompletableFuture.runAsync(() -> {
            try {
                // Вызываем сервис обнаружения
                // todo
                detectedCorners = detectionService.detectDocumentCorners(currentMatImage);

                // Возвращаемся в поток JavaFX для обновления UI
                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    cornersCountLabel.setText("Углов обнаружено: " + detectedCorners.size());
                    statusLabel.setText("✅ Углы обнаружены: " + detectedCorners.size());

                    logger.info("Углы успешно обнаружены: {}", detectedCorners);

                    // Показываем информацию об углах
                    for (CornerPoint cp : detectedCorners) {
                        logger.info("  - {}", cp);
                    }
                });
            } catch (Exception e) {
                logger.error("Ошибка при обнаружении углов", e);
                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText("❌ Ошибка при обнаружении углов: " + e.getMessage());
                });
            }
        });



    }

    private void handleNormalize() {
        logger.info(">>> Нажата кнопка 'Нормализовать'");

        // Проверяем, загружено ли изображение
        if (currentMatImage == null) {
            statusLabel.setText("❌ Сначала загрузите изображение!");
            return;
        }

        // Проверяем, обнаружены ли углы
        if (detectedCorners == null || detectedCorners.isEmpty()) {
            statusLabel.setText("❌ Сначала обнаружьте углы документа!");
            logger.warn("Попытка нормализовать без обнаруженных углов");
            return;
        }

        // Показываем индикатор загрузки
        progressIndicator.setVisible(true);
        statusLabel.setText("⏳ Нормализую документ...");
        logger.info("Начало нормализации документа");

        // Запускаем в отдельном потоке (чтобы UI не зависал)
        CompletableFuture.runAsync(() -> {
            try {
                // Вызываем сервис трансформации
                normalizedImage = transformService.perspectiveTransform(
                        currentMatImage,
                        detectedCorners
                );

                // Конвертируем Mat → Image для отображения
                Image resultImage = convertMatToImage(normalizedImage);

                // Возвращаемся в поток JavaFX для обновления UI
                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);

                    if (resultImage != null) {
                        resultImageView.setImage(resultImage);
                        statusLabel.setText("✅ Документ нормализован!");
                        logger.info("Документ успешно нормализован. Размер: {} x {}",
                                (int)resultImage.getWidth(), (int)resultImage.getHeight());

                        // Переходим на вкладку с результатом
                        tabPane.getSelectionModel().select(1);
                    } else {
                        statusLabel.setText("❌ Ошибка при конвертировании результата");
                    }
                });
            } catch (Exception e) {
                logger.error("Ошибка при нормализации", e);
                javafx.application.Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText("❌ Ошибка при нормализации: " + e.getMessage());
                });
            }
        });
    }

    private void handleSaveNormalized() {
        logger.info(">>> Нажата кнопка 'Сохранить результат'");

        if (normalizedImage == null || normalizedImage.empty()) {
            statusLabel.setText("❌ Нет результата для сохранения!");
            logger.warn("Попытка сохранить без результата нормализации");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить нормализованное изображение");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("PNG файлы", "*.png"),
                new javafx.stage.FileChooser.ExtensionFilter("JPG файлы", "*.jpg", "*.jpeg"),
                new javafx.stage.FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        fileChooser.setInitialFileName("normalized.png");

        javafx.stage.Stage stage = new javafx.stage.Stage();
        java.io.File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try {
                // Определяем расширение файла
                String fileName = selectedFile.getAbsolutePath();
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

                // Кодируем в файл
                boolean success;
                if (extension.equals("png")) {
                    success = Imgcodecs.imwrite(fileName, normalizedImage);
                } else if (extension.equals("jpg") || extension.equals("jpeg")) {
                    success = Imgcodecs.imwrite(fileName, normalizedImage);
                } else {
                    // По умолчанию используем PNG
                    success = Imgcodecs.imwrite(fileName + ".png", normalizedImage);
                }

                if (success) {
                    statusLabel.setText("✅ Результат сохранён: " + selectedFile.getAbsolutePath());
                    logger.info("Результат нормализации сохранён: {}", selectedFile.getAbsolutePath());
                } else {
                    statusLabel.setText("❌ Ошибка при сохранении файла");
                    logger.error("Ошибка при сохранении файла OpenCV");
                }
            } catch (Exception e) {
                statusLabel.setText("❌ Ошибка: " + e.getMessage());
                logger.error("Ошибка при сохранении нормализованного изображения", e);
            }
        }
    }

    private Mat convertImageToMat(Image fxImage) {
        logger.debug("Используем БЕЗОПАСНЫЙ способ конвертации Image → Mat");

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);

            if (bufferedImage == null) return null;

            // Всегда конвертируем в стандартный RGB формат
            BufferedImage rgbImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR
            );

            java.awt.Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(bufferedImage, 0, 0, null);
            g2d.dispose();

            // Создаём Mat
            Mat mat = new Mat(rgbImage.getHeight(), rgbImage.getWidth(),
                    org.opencv.core.CvType.CV_8UC3);

            byte[] data = ((java.awt.image.DataBufferByte) rgbImage.getRaster()
                    .getDataBuffer()).getData();

            mat.put(0, 0, data);

            logger.info("✅ Image успешно конвертирована (безопасный способ)");
            return mat;

        } catch (Exception e) {
            logger.error("❌ Ошибка в безопасном конвертировании", e);
            return null;
        }
    }

    private Image convertMatToImage(Mat mat) {
        logger.debug("Конвертирую Mat → Image");

        try {
            // Кодируем Mat в байты PNG
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".png", mat, matOfByte);

            // Конвертируем в Image
            byte[] imageData = matOfByte.toArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
            Image image = new Image(byteArrayInputStream);

            return image;
        } catch (Exception e) {
            logger.error("Ошибка при конвертировании Mat → Image", e);
            return null;
        }
    }
    public static void main(String[] args) {
        logger.info("========== ЗАПУСК ГЛАВНОГО МЕТОДА ==========");
        launch(args);
    }
}
