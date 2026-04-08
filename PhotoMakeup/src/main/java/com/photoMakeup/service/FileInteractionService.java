package com.photoMakeup.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photoMakeup.model.CustomRectangle;
import com.photoMakeup.service.utils.FileFormatter;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static javafx.embed.swing.SwingFXUtils.fromFXImage;

public class FileInteractionService implements FileFormatter {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File file;
    private Label fileNameLabel;
    private final CanvasPanel canvasPanel;

    public FileInteractionService(CanvasPanel canvasPanel) {
        this.canvasPanel = canvasPanel;
    }

    public void setFileNameLabel(Label fileNameLabel) {
        this.fileNameLabel = fileNameLabel;
    }

    public void openLoadImageDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберете файл с изображнием");
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
                canvasPanel.setCurrentImage(fxImage, true);
                canvasPanel.redraw();
                fileNameLabel.setText(selectedFile.getAbsolutePath());
            } catch (Exception e) {

            }
        }
    }

    public void openSaveImageDialog() {

        BufferedImage image = fromFXImage(canvasPanel.getViewModel().getCurrentImage(), null);

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить измененное изображение");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("PNG файлы", "*.png"),
                new javafx.stage.FileChooser.ExtensionFilter("JPG файлы", "*.jpg", "*.jpeg"),
                new javafx.stage.FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        fileChooser.setInitialFileName(String.format("%s.png", file.getName().substring(0, file.getName().lastIndexOf("."))));

        javafx.stage.Stage stage = new javafx.stage.Stage();
        java.io.File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try {
                ImageIO.write(image, "png", selectedFile);
            } catch (Exception e) {
            }
        }
    }

    public void openSaveMarksDialog() {
        if (canvasPanel.getViewModel().marksCountProperty().get() > 0){
            System.out.println("⚠\uFE0F  Нет разметок для сохранения!");
        }
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
                saveMarks(canvasPanel.getViewModel().getRectangles(), selectedFile);
            } catch (Exception e) {

            }
        }
    }

    public void openLoadMarksDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить разметку");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON файлы", "*.json")
        );

        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                List<CustomRectangle> rectangles = loadMarks(selectedFile);
                canvasPanel.getViewModel().setRectangles(rectangles);
                canvasPanel.redraw();
            } catch (IOException e) {
            }
        }
    }
}
