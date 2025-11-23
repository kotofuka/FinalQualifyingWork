package com.photoMakeup.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photoMakeup.model.Rectangle;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
                Image fxImage = new Image(selectedFile.toURI().toString());
                canvasPanel.setImage(fxImage);
                logger.info("Изображение загружено: {}", selectedFile.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Ошибка при загрузке изображения", e);
            }
        }
    }

    public void saveMarksDialog(CanvasPanel canvasPanel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить разметку");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON файлы", "*.json")
        );
        fileChooser.setInitialFileName("marks.json");

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
