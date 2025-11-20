package com.photoMakeup;

import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class PhotoMakeupApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();

        HBox toolbar = createToolbar();
        root.setTop(toolbar);


    }

    private HBox createToolbar() {
        return new HBox();
    }
}
