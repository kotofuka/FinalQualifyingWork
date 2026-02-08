package com.photoMakeup.service;

import ch.qos.logback.core.encoder.JsonEscapeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photoMakeup.model.Rectangle;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File file;

    public void openImageDialog(CanvasPanel canvasPanel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.jpeg", "*.png", "*.bmp"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                file = selectedFile;
                Image fxImage = new Image(file.toURI().toString());
                canvasPanel.setImage(fxImage);
                logger.info("Изображение загружено: {}", selectedFile.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Ошибка при загрузке изображения", e);
            }
        }
    }

    public void saveImage(Mat normalizedImage) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить нормализованное изображение");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("PNG файлы", "*.png"),
                new javafx.stage.FileChooser.ExtensionFilter("JPG файлы", "*.jpg", "*.jpeg"),
                new javafx.stage.FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        fileChooser.setInitialFileName(String.format("%s_normalized.png", file.getName().substring(0, file.getName().lastIndexOf("."))));

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
                    success = Imgcodecs.imwrite(fileName + ".png", normalizedImage);
                }

            } catch (Exception e) {
                logger.error("Ошибка при сохранении нормализованного изображения", e);
            }
        }
    }

    public void saveMarksDialog(CanvasPanel canvasPanel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить разметку");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON файлы", "*.json")
        );
        fileChooser.setInitialFileName(String.format("%s_marks.json", file.getName().substring(0, file.getName().lastIndexOf("."))));

        Stage stage = new Stage();
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try {
                saveMarks(canvasPanel.getRectangles(), selectedFile);
                logger.info("Разметка сохранена: {}", selectedFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Ошибка при сохранении разметки", e);
            }
        }
    }

    public void loadMarksDialog(CanvasPanel canvasPanel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить разметку");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON файлы", "*.json")
        );

        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                List<Rectangle> rectangles = loadMarks(selectedFile);
                canvasPanel.setRectangles(rectangles);
                logger.info("Разметка успешно загружена: {}", selectedFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Ошибка при загрузке разметки", e);
            }
        }
    }

    private void saveMarks(List<Rectangle> rectangles, File file) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();

        for (Rectangle rectangle : rectangles) {
            Map<String, Object> rectangleMap = Map.of(
                    "point1", Map.of("x", rectangle.getX1(), "y", rectangle.getY1()),
                    "point2", Map.of("x", rectangle.getX2(), "y", rectangle.getY2())
            );
            data.add(rectangleMap);
        }
        Map<String, Object> result = Map.of(
                "marks", data,
                "count", rectangles.size()
        );

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(result, writer);
        }
    }

    private List<Rectangle> loadMarks(File file) throws IOException {
        List<Rectangle> rectangles = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> data = gson.fromJson(reader, Map.class);
            List<Map<String, Object>> marks = (List<Map<String, Object>>) data.get("marks");

            if (marks != null) {
                for (Map<String, Object> mark : marks) {
                    Map<String, Number> point1 = (Map<String, Number>) mark.get("point1");
                    Map<String, Number> point2 = (Map<String, Number>) mark.get("point2");

                    double x1 = point1.get("x").doubleValue();
                    double y1 = point1.get("y").doubleValue();
                    double x2 = point2.get("x").doubleValue();
                    double y2 = point2.get("y").doubleValue();

                    rectangles.add(new Rectangle(x1, y1, x2, y2));
                }
            }
        }
        return rectangles;
    }
}
