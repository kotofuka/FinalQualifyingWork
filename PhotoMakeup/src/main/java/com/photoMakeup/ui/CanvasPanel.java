package com.photoMakeup.ui;

import com.photoMakeup.model.Rectangle;
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
    private Canvas canvas;
    private Image currentImage;
    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;

    private Rectangle currentRectangle;
    private double startX, startY;

    private List<Rectangle> rectangles = new ArrayList<>();

    private IntegerProperty marksCount = new SimpleIntegerProperty(0);

    private ObjectProperty<double[]> mouseCoord = new SimpleObjectProperty<>(null);

    private boolean isPanning = false;
    private double panStartX, panStartY;

    private double lastZoom = 1.0;

    public CanvasPanel() {
        canvas = new Canvas(900, 600);
        this.getChildren().add(canvas);

        setupMouseHandlers();
        setupScrollHandler();
        setupKeyboardHandler();

        redraw();
    }

    public Image getCurrentImage() {
        return currentImage;
    }

    public void setImage(Image image) {
        currentImage = image;
        zoom = 1.0;
        lastZoom = 1.0;
        panX = 0;
        panY = 0;
        rectangles.clear();
        marksCount.set(0);
        mouseCoord.set(null);
        redraw();
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseExited(this::handleMouseExited);
    }

    private void setupScrollHandler() {
        canvas.setOnScroll(this::handleScroll);
    }

    private void setupKeyboardHandler() {
        canvas.setFocusTraversable(true);
    }

    private void handleMousePressed(MouseEvent event) {
        canvas.requestFocus();
        updateMouseCoordinates(event.getX(), event.getY());

        if(event.isControlDown() || event.getButton() == MouseButton.MIDDLE){
            isPanning = true;
            panStartX = event.getX();
            panStartY = event.getY();
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Начало рисования прямоугольника
            startX = (event.getX() - panX) / zoom;
            startY = (event.getY() - panY) / zoom;
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Удаление прямоугольника
            deleteRectangleAt(event.getX(), event.getY());
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        updateMouseCoordinates(event.getX(), event.getY());

        if(isPanning){
            panX += event.getX() - panStartX;
            panY += event.getY() - panStartY;
            panStartX = event.getX();
            panStartY = event.getY();
            redraw();
        } else if (event.getButton() == MouseButton.PRIMARY) {
            double endX = (event.getX() - panX) / zoom;
            double endY = (event.getY() - panY) / zoom;

            double x1 = Math.min(startX, endX);
            double y1 = Math.min(startY, endY);
            double x2 = Math.max(startX, endX);
            double y2 = Math.max(startY, endY);

            currentRectangle = new Rectangle(x1, y1, x2 , y2);
            redraw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        updateMouseCoordinates(event.getX(), event.getY());

        if(isPanning){
            isPanning = false;
        } else if (event.getButton() == MouseButton.PRIMARY && currentRectangle != null) {
            if (currentRectangle.getWidth() > 5 && currentRectangle.getHeight() > 5) {
                rectangles.add(currentRectangle);
                marksCount.set(rectangles.size());
            }
            currentRectangle = null;
            redraw();
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (currentImage == null) return;

        double mouseImageX = (event.getX() - panX) / zoom;
        double mouseImageY = (event.getY() - panY) / zoom;

        double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
        zoom = Math.max(0.1, Math.min(5.0, zoom * zoomFactor));

        panX = event.getX() - (mouseImageX * zoom);
        panY = event.getY() - (mouseImageY * zoom);

        lastZoom = zoom;

        updateMouseCoordinates(event.getX(), event.getY());
        redraw();
        event.consume();
    }

    private void handleMouseMoved(MouseEvent event){
        updateMouseCoordinates(event.getX(), event.getY());
    }

    private void handleMouseExited(MouseEvent event) {
        mouseCoord.set(null);  // Сбрасываем координаты
    }

    private void updateMouseCoordinates(double screenX, double screenY) {
        if (currentImage == null) {
            mouseCoord.set(null);
            return;
        }

        double currentZoom = zoom;

        // Координаты в системе изображения
        double imageX = (screenX - panX) / currentZoom;
        double imageY = (screenY - panY) / currentZoom;

        // Проверяем, находится ли курсор внутри изображения
        if (imageX >= 0 && imageX <= currentImage.getWidth() &&
                imageY >= 0 && imageY <= currentImage.getHeight()) {
            mouseCoord.set(new double[]{imageX, imageY, screenX, screenY});
        } else {
            mouseCoord.set(null);
        }
    }

    private void deleteRectangleAt(double x, double y) {
        for (int i = rectangles.size() - 1; i >= 0; i--) {
            Rectangle rectangle = rectangles.get(i);
            double screenX1 = rectangle.getX1() * zoom + panX;
            double screenY1 = rectangle.getY1() * zoom + panY;
            double screenX2 = rectangle.getX2() * zoom + panX;
            double screenY2 = rectangle.getY2() * zoom + panY;

            if (x >= screenX1 && x <= screenX2 && y >= screenY1 && y <= screenY2) {
                rectangles.remove(i);
                marksCount.set(rectangles.size());
                redraw();
                return;
            }
        }
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.web("#f5f5f5"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTransform(1, 0, 0, 1, 0, 0);

        if (currentImage != null) {
            double scaledWidth = currentImage.getWidth() * zoom;
            double scaledHeight = currentImage.getHeight() * zoom;
            gc.drawImage(currentImage, panX, panY, scaledWidth, scaledHeight);
        }

        gc.setStroke(Color.web("#ff0000"));
        gc.setLineWidth(2.0);
        for (Rectangle rectangle : rectangles) {
            drawRectangle(gc, rectangle);
        }

        if (currentRectangle != null) {
            gc.setStroke(Color.web("#ff6600"));
            gc.setLineWidth(2.0);
            gc.setLineDashes(5.0);
            drawRectangle(gc, currentRectangle);
            gc.setLineDashes(0);
        }
    }

    private void drawRectangle(GraphicsContext gc, Rectangle rectangle) {
        double screenX1 = rectangle.getX1() * zoom + panX;
        double screenY1 = rectangle.getY1() * zoom + panY;
        double screenX2 = rectangle.getX2() * zoom + panX;
        double screenY2 = rectangle.getY2() * zoom + panY;

        double width = screenX2 - screenX1;
        double height = screenY2 - screenY1;

        // Для pixel-perfect рисования
        double x = snapX(screenX1);
        double y = snapY(screenY1);

        gc.strokeRect(x, y, width, height);
    }

    private double snapX(double x) {
        return ((int) x) + 0.5;
    }

    private double snapY(double y) {
        return ((int) y) + 0.5;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        Bounds bounds = getLayoutBounds();
        canvas.setWidth(bounds.getWidth());
        canvas.setHeight(bounds.getHeight());
        redraw();
    }

    public double getZoom() {
        return zoom;
    }

    public double getPanX() {
        return panX;
    }



    public double getPanY() {
        return panY;
    }

    public void setZoom(double zoom){
        this.zoom = zoom;
        redraw();
    }

    public ObjectProperty<double[]> mouseCoordProperty() {
        return mouseCoord;
    }

    public void clearMarks(){
        rectangles.clear();
        currentRectangle = null;
        marksCount.set(0);
        redraw();
    }

    public List<Rectangle> getRectangles() {
        return new ArrayList<>(rectangles);
    }

    public void setRectangles(List<Rectangle> rectangles){
        this.rectangles = new ArrayList<>(rectangles);
        marksCount.set(rectangles.size());
        redraw();
    }

    public IntegerProperty getMarksCount(){
        return marksCount;
    }
}
