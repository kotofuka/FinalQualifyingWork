package com.photoMakeup.ui;

import com.photoMakeup.model.CanvasViewModel;
import com.photoMakeup.model.CustomRectangle;
import com.photoMakeup.service.CanvasInteractionService;
import com.photoMakeup.service.CanvasRenderingService;
import com.photoMakeup.service.ImageAnnotationService;
import com.photoMakeup.service.ImageNavigationService;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class CanvasPanel extends StackPane {
//    private Canvas canvas;
//    private Image currentImage;
//
//    private double zoom = 1.0;
//    private double panX = 0, panY = 0;
////    private double lastZoom = 1.0;
//
//    private CustomRectangle currentRectangle;
//    private double startX, startY;
//
//    private List<CustomRectangle> rectangles = new ArrayList<>();
//
//    private IntegerProperty marksCount = new SimpleIntegerProperty(0);
//    private ObjectProperty<double[]> mouseCoordinates = new SimpleObjectProperty<>(null);
//
//    private boolean isPanning = false;
//    private double panStartX, panStartY;
//
//    public CanvasPanel() {
//        canvas = new Canvas(900, 600);
//        getChildren().add(canvas);
//
//        setupMouseHandlers();
//        setupScrollHandler();
//        setupKeyboardHandler();
//
//        redraw();
//    }
//
//    private void setupMouseHandlers() {
//        canvas.setOnMousePressed(this::handleMousePressed);
//        canvas.setOnMouseDragged(this::handleMouseDragged);
//        canvas.setOnMouseReleased(this::handleMouseReleased);
//        canvas.setOnMouseMoved(this::handleMouseMoved);
//        canvas.setOnMouseExited(this::handleMouseExited);
//    }
//
//    private void setupScrollHandler() {
//        canvas.setOnScroll(this::handleScroll);
//    }
//
//    private void setupKeyboardHandler(){
//        canvas.setFocusTraversable(true);
//    }
//
//    private void redraw() {
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//
//        gc.setFill(Color.web("#f5f5f5"));
//        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//
//        gc.setTransform(1, 0, 0, 1, 0, 0);
//
//        if (currentImage != null){
//            double scaleWidth = currentImage.getWidth() * zoom;
//            double scaleHeight = currentImage.getHeight() * zoom;
//            gc.drawImage(currentImage, panX, panY, scaleWidth, scaleHeight);
//        }
//        gc.setStroke(Color.web("#ff0000"));
//        gc.setLineWidth(2.0);
//        for(CustomRectangle rectangle: rectangles){
//            drawRectangle(gc, rectangle);
//        }
//
//        if (currentRectangle != null){
//            gc.setStroke(Color.web("#ff6600"));
//            gc.setLineWidth(2.0);
//            gc.setLineDashes(5.0);
//            drawRectangle(gc, currentRectangle);
//            gc.setLineDashes(0);
//        }
//    }
//
//    private void drawRectangle(GraphicsContext gc, CustomRectangle rectangle) {
//        double screenX1 = rectangle.getX1() * zoom + panX;
//        double screenY1 = rectangle.getY1() * zoom + panY;
//        double screenX2 = rectangle.getX2() * zoom + panX;
//        double screenY2 = rectangle.getY2() * zoom + panY;
//
//        double width = screenX2 - screenX1;
//        double height = screenY2 - screenY1;
//
//        double x = snapPos(screenX1);
//        double y = snapPos(screenY1);
//
//        gc.strokeRect(x, y, width, height);
//    }
//
//    private double snapPos(double x) {
//        return ((int) x) + 0.5;
//    }
//
//    public Image getCurrentImage() {return currentImage;}
//
//    public void setCurrentImage(Image image) {
//        currentImage = image;
//        zoom = 1.0;
//        panX = 0;
//        panY = 0;
//        rectangles.clear();
//        marksCount.set(0);
//        mouseCoordinates.set(null);
//        redraw();
//    }
//
//    public double getZoom() {return zoom;}
//
//    public void setZoom(double zoom) {
//        this.zoom = zoom;
//        redraw();
//    }
//
//    public double getPanX() {return panX;}
//
//    public double getPanY() {return panY;}
//
//    public ObjectProperty<double[]> mouseCoordinates() {return mouseCoordinates;}
//
//    public void clearMarks(){
//        rectangles.clear();
//        currentRectangle = null;
//        marksCount.set(0);
//        redraw();
//    }
//
//    public List<CustomRectangle> getRectangles() {return new ArrayList<>(rectangles);}
//
//    public void setRectangles(List<CustomRectangle> rectangles) {
//        this.rectangles = new ArrayList<>(rectangles);
//        marksCount.set(rectangles.size());
//        redraw();
//    }
//
//    public IntegerProperty getMarksCount(){return marksCount;}
//
//    @Override
//    protected void layoutChildren() {
//        super.layoutChildren();
//        Bounds bounds = getLayoutBounds();
//        canvas.setWidth(bounds.getWidth());
//        canvas.setHeight(bounds.getHeight());
//        redraw();
//    }
//
//    private void handleMousePressed(MouseEvent event) {
//        canvas.requestFocus();
//        updateMouseCoordinates(event.getX(), event.getY());
//
//        if (event.isControlDown() || event.getButton() == MouseButton.MIDDLE){
//            // перемещаемся по картинке
//            isPanning = true;
//            panStartX = event.getX();
//            panStartY = event.getY();
//        } else if (event.getButton() == MouseButton.PRIMARY){
//            // Начало рисования прямоугольника (метки)
//            startX = (event.getX() - panX) / zoom;
//            startY = (event.getY() - panY) / zoom;
//        } else if (event.getButton() == MouseButton.SECONDARY){
//            // Удаление прямоугольника
//            deleteRectangleAt(event.getX(), event.getY());
//        }
//    }
//
//    private void updateMouseCoordinates(double screenX, double screenY) {
//        if (currentRectangle == null){
//            mouseCoordinates.set(null);
//            return;
//        }
//
//        double currentZoom = zoom;
//
//        double imageX = (screenX - panX) / currentZoom;
//        double imageY = (screenY - panY) / currentZoom;
//
//        if (imageX >= 0 && imageY >= 0 && imageX <= currentImage.getWidth() && imageY <= currentImage.getHeight()){
//            mouseCoordinates.set(new double[]{imageX, imageY, screenX, screenY});
//        } else {
//            mouseCoordinates.set(null);
//        }
//    }
//
//    private void deleteRectangleAt(double x, double y){
//        for (CustomRectangle rectangle: rectangles){
//            double screenX1 = rectangle.getX1() * zoom + panX;
//            double screenY1 = rectangle.getY1() * zoom + panY;
//            double screenX2 = rectangle.getX2() * zoom + panX;
//            double screenY2 = rectangle.getY2() * zoom + panY;
//
//            if (x >= screenX1 && x <= screenX2 && y >= screenY1 && y <= screenY2){
//                rectangles.remove(rectangle);
//                marksCount.set(rectangles.size());
//                redraw();
//                return;
//            }
//        }
//    }
//
//    private void handleMouseDragged(MouseEvent event){
//        updateMouseCoordinates(event.getX(), event.getY());
//
//        if (isPanning) {
//            panX += event.getX() - panStartX;
//            panY += event.getY() - panStartY;
//            panStartX = event.getX();
//            panStartY = event.getY();
//            redraw();
//        } else if (event.getButton() == MouseButton.PRIMARY){
//            double endX = (event.getX() - panX) / zoom;
//            double endY = (event.getY() - panY) / zoom;
//
//            currentRectangle = new CustomRectangle(startX, startY, endX, endY);
//            redraw();
//        }
//    }
//
//    private void handleMouseReleased(MouseEvent event){
//        updateMouseCoordinates(event.getX(), event.getY());
//
//        if (isPanning){
//            isPanning = false;
//        } else if (event.getButton() == MouseButton.PRIMARY && currentRectangle != null){
//            if (currentRectangle.getWidth() > 5 && currentRectangle.getHeight() > 5){
//                rectangles.add(currentRectangle);
//                marksCount.set(rectangles.size());
//            }
//            currentRectangle = null;
//            redraw();
//        }
//    }
//
//    private void handleScroll(ScrollEvent event){
//        if (currentImage == null) return;
//
//        double mouseImageX = (event.getX() - panX) / zoom;
//        double mouseImageY = (event.getY() - panY) / zoom;
//        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
//
//        zoom = Math.max(0.1, Math.min(5.0, zoom * zoomFactor));
//
//        panX = event.getX() - (mouseImageX * zoom);
//        panY = event.getY() - (mouseImageY * zoom);
//
//        updateMouseCoordinates(event.getX(), event.getY());
//        redraw();
//        event.consume();
//    }
//
//    private void handleMouseMoved(MouseEvent event){
//        updateMouseCoordinates(event.getX(), event.getY());
//    }
//
//    private void handleMouseExited(MouseEvent event){
//        mouseCoordinates.set(null);
//    }

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
