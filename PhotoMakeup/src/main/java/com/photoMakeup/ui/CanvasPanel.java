package com.photoMakeup.ui;

import com.photoMakeup.model.CanvasViewModel;
import com.photoMakeup.service.CanvasInteractionService;
import com.photoMakeup.service.CanvasRenderingService;
import com.photoMakeup.service.ImageAnnotationService;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

public class CanvasPanel extends StackPane {
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final ImageAnnotationService annotationService;
    private final CanvasInteractionService interactionService;
    private final CanvasRenderingService renderingService;

    private final CanvasViewModel viewModel;

    public CanvasPanel() {
        viewModel = new CanvasViewModel();

        System.out.println("creating CanvasPanel: " + viewModel);
        canvas = new Canvas(900, 600);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        this.renderingService = new CanvasRenderingService(viewModel, canvas);
        this.annotationService = new ImageAnnotationService(viewModel, renderingService);
        this.interactionService = new CanvasInteractionService(viewModel, canvas, renderingService, annotationService);

        renderingService.render();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        Bounds bounds = getLayoutBounds();
        canvas.setWidth(bounds.getWidth());
        canvas.setHeight(bounds.getHeight());
        renderingService.render();
    }

    public void setCurrentImage(Image image) {
        viewModel.setCurrentImage(image);
        renderingService.render();
    }
}
