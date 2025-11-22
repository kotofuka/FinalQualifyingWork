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
import java.util.List;

// TODO нужно переделать этот файл
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
                canvasPanel.loadImage(fxImage);
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
                List<Rectangle> rectangles = canvasPanel.getRectangles();
                String json = gson.toJson(rectangles);

                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(json);
                }
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
                try (FileReader reader = new FileReader(selectedFile)) {
                    Rectangle[] rectanglesArray = gson.fromJson(reader, Rectangle[].class);
                    logger.info("Разметка загружена: {} прямоугольников", rectanglesArray.length);
                }
            } catch (IOException e) {
                logger.error("Ошибка при загрузке разметки", e);
            }
        }
    }
}
