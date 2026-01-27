package com.photoMakeup.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileInteractionService {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File file;

    public void openImageDialog(CanvasPanel canvasPanel) {
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
                canvasPanel.setCurrentImage(fxImage);
            } catch (Exception e) {

            }
        }
    }
}
