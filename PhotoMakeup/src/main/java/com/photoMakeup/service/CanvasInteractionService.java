package com.photoMakeup.service;

import com.photoMakeup.model.CanvasViewModel;
import com.photoMakeup.model.CustomRectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CanvasInteractionService {
    private final CanvasViewModel viewModel;
    private final Canvas canvas;

    private final CanvasRenderingService renderingService;
    private final ImageAnnotationService annotationService;

    public CanvasInteractionService(CanvasViewModel viewModel, Canvas canvas, CanvasRenderingService renderingService,
                                    ImageAnnotationService imageAnnotationService) {
        this.viewModel = viewModel;
        this.canvas = canvas;
        this.renderingService = renderingService;
        this.annotationService = imageAnnotationService;

        setupHandlers();
    }

    private void setupHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseExited(this::handleMouseExited);

        canvas.setOnScroll(this::handleScroll);

        canvas.setFocusTraversable(true);
    }

    private void handleMousePressed(MouseEvent event) {
        canvas.requestFocus();
        updateMouseCoordinates(event.getX(), event.getY());

        if (event.isControlDown() || event.getButton() == MouseButton.MIDDLE){
            // перемещаемся по картинке
            viewModel.setPanning(true);
            System.out.println("Start panStart:" + viewModel.getPanStartX() + " | " + viewModel.getPanStartY() );
            viewModel.setPanStart(event.getX(), event.getY());
        } else if (event.getButton() == MouseButton.PRIMARY){
            // Начало рисования прямоугольника (метки)
            viewModel.setStart((event.getX() - viewModel.getPanX()) / viewModel.getZoom(),
                                (event.getY() - viewModel.getPanY()) / viewModel.getZoom());

        } else if (event.getButton() == MouseButton.SECONDARY){
            // Удаление прямоугольника
            annotationService.deleteRectangleAt(event.getX(), event.getY());
        }
    }

    private void updateMouseCoordinates(double screenX, double screenY) {
        if (viewModel.getCurrentRectangle() == null){
            viewModel.mouseCoordinatesProperty().set(null);
            return;
        }

        double currentZoom = viewModel.getZoom();

        double imageX = (screenX - viewModel.getPanX()) / currentZoom;
        double imageY = (screenY - viewModel.getPanY()) / currentZoom;

        if (imageX >= 0 && imageY >= 0 && imageX <= viewModel.getCurrentImageWidth()
                && imageY <= viewModel.getCurrentImageHeight()){
            viewModel.mouseCoordinatesProperty().set(new double[]{imageX, imageY, screenX, screenY});
        } else {
            viewModel.mouseCoordinatesProperty().set(null);
        }
    }

    private void handleMouseDragged(MouseEvent event){
        updateMouseCoordinates(event.getX(), event.getY());

        if (viewModel.isPanning()) {

            viewModel.setPan(viewModel.getPanX() + event.getX() - viewModel.getPanStartX(),
                            viewModel.getPanY() + event.getY() - viewModel.getPanStartY());
            viewModel.setPanStart(event.getX(), event.getY());

            renderingService.render();
        } else if (event.getButton() == MouseButton.PRIMARY){
            double endX = (event.getX() - viewModel.getPanX()) / viewModel.getZoom();
            double endY = (event.getY() - viewModel.getPanY()) / viewModel.getZoom();

            viewModel.setCurrentRectangle(new CustomRectangle(viewModel.getStartX(), viewModel.getStartY(), endX, endY));
            renderingService.render();
        }
    }

    private void handleMouseReleased(MouseEvent event){
        updateMouseCoordinates(event.getX(), event.getY());
        var currentRectangle = viewModel.getCurrentRectangle();
        if (viewModel.isPanning()){
            System.out.println("finish panStart:" + viewModel.getPanStartX() + " | " + viewModel.getPanStartY() );
            viewModel.setPanning(false);
        } else if (event.getButton() == MouseButton.PRIMARY && currentRectangle != null){
            if (currentRectangle.getWidth() > 5 && currentRectangle.getHeight() > 5){
                viewModel.addRectangle(currentRectangle);

                viewModel.marksCountProperty().set(viewModel.getRectangles().size());
            }
            viewModel.setCurrentRectangle(null);
            renderingService.render();
            System.out.println("rectangles size: " + viewModel.getRectangles().size());
        }
    }

    private void handleMouseMoved(MouseEvent event){
        updateMouseCoordinates(event.getX(), event.getY());
    }

    private void handleMouseExited(MouseEvent event){
        viewModel.mouseCoordinatesProperty().set(null);
    }

    private void handleScroll(ScrollEvent event){
        if (viewModel.getCurrentImage() == null) return;

        var zoom = viewModel.getZoom();
        double mouseImageX = (event.getX() - viewModel.getPanX()) / zoom;
        double mouseImageY = (event.getY() - viewModel.getPanY()) / zoom;
        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;

        viewModel.setZoom(Math.max(0.1, Math.min(5.0, zoom * zoomFactor)));

        zoom = viewModel.getZoom();

        viewModel.setPan(event.getX() - (mouseImageX * zoom), event.getY() - (mouseImageY * zoom));

        updateMouseCoordinates(event.getX(), event.getY());
        renderingService.render();
        event.consume();
    }
}
