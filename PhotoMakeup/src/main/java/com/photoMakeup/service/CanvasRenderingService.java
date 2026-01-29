package com.photoMakeup.service;

import com.photoMakeup.model.CanvasViewModel;
import com.photoMakeup.model.CustomRectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CanvasRenderingService {
    private final CanvasViewModel viewModel;
    private final Canvas canvas;
    private final GraphicsContext gc;

    public CanvasRenderingService(CanvasViewModel viewModel, Canvas canvas) {
        this.viewModel = viewModel;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

    }

    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.web("#f5f5f5"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTransform(1, 0, 0, 1, 0, 0);
        if (viewModel.getCurrentImage() != null){
            double scaledWidth = viewModel.getCurrentImageWidth() * viewModel.getZoom();
            double scaledHeight = viewModel.getCurrentImageHeight() * viewModel.getZoom();
            gc.drawImage(viewModel.getCurrentImage(), viewModel.getPanX(), viewModel.getPanY(), scaledWidth, scaledHeight);
        }

        gc.setStroke(Color.web("#ff0000"));
        gc.setLineWidth(2.0);
        for(CustomRectangle rectangle: viewModel.getRectangles()){
            drawRectangle(gc, rectangle);
        }

        var currentRectangle = viewModel.getCurrentRectangle();
        if (currentRectangle != null){
            gc.setStroke(Color.web("#ff6600"));
            gc.setLineWidth(2.0);
            gc.setLineDashes(5.0);
            drawRectangle(gc, currentRectangle);
            gc.setLineDashes(0);
        }
    }

    private void drawRectangle(GraphicsContext gc, CustomRectangle rectangle) {
        double screenX1 = rectangle.getX1() * viewModel.getZoom() + viewModel.getPanX();
        double screenY1 = rectangle.getY1() * viewModel.getZoom() + viewModel.getPanY();
        double screenX2 = rectangle.getX2() * viewModel.getZoom() + viewModel.getPanX();
        double screenY2 = rectangle.getY2() * viewModel.getZoom() + viewModel.getPanY();

        double width = screenX2 - screenX1;
        double height = screenY2 - screenY1;

        double x = snapPos(screenX1);
        double y = snapPos(screenY1);

        gc.strokeRect(x, y, width, height);
    }

    private double snapPos(double x) {
        return ((int) x) + 0.5;
    }
}
