package com.photoMakeup.ui;

import com.photoMakeup.model.Rectangle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    private final Canvas canvas;
    private Image currentImage;                        // Текущее загруженное изображение
    private final List<Rectangle> rectangles;         // Все нарисованные прямоугольники
    private Rectangle currentRectangle;         // Прямоугольник, который рисуется сейчас
    private double startX, startY;              // Начальные координаты (где кликнули)
    private double zoom = 1.0;                  // Уровень масштабирования
    private double panX = 0, panY = 0;          // Смещение при панораме
    private final IntegerProperty marksCount;         // Количество разметок (для UI)

    public CanvasPanel() {
        canvas = new Canvas(800, 600);
        this.getChildren().add(canvas);
        rectangles = new ArrayList<Rectangle>();
        marksCount = new SimpleIntegerProperty(0);

        setupMouseHandlers();
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseClicked(this::handleMouseClicked);
        canvas.setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();

        double imageX = (startX - panX) / zoom;
        double imageY = (startY - panY) / zoom;

        currentRectangle = new Rectangle(imageX, imageY, imageX, imageY);
    }

    private void handleMouseDragged(MouseEvent event) {
        if (currentRectangle != null && event.getButton() == MouseButton.PRIMARY) {

            double currentScreenX = event.getX();
            double currentScreenY = event.getY();

            double currentImageX = (currentScreenX - panX) / zoom;
            double currentImageY = (currentScreenY - panY) / zoom;

            double imageStartX = (startX - panX) / zoom;
            double imageStartY = (startY - panY) / zoom;

            currentRectangle = new Rectangle(
                    imageStartX,
                    imageStartY,
                    currentImageX,
                    currentImageY
            );

            draw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (currentRectangle != null && event.getButton() == MouseButton.PRIMARY) {
            if (currentRectangle.getWidth() > 5 && currentRectangle.getHeight() > 5) {
                rectangles.add(currentRectangle);
                marksCount.set(rectangles.size());
            }
            currentRectangle = null;
            draw();
        }
    }

    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            double clickX = (event.getX() - panX) / zoom;
            double clickY = (event.getY() - panY) / zoom;

            for (Rectangle rect : new ArrayList<>(rectangles)) {
                if (clickX >= rect.getX1() && clickX <= rect.getX2() &&
                        clickY >= rect.getY1() && clickY <= rect.getY2()) {
                    rectangles.remove(rect);
                    marksCount.set(rectangles.size());
                    break;
                }
            }
            draw();
        }
    }

    private void handleScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        double factor = delta > 0 ? 1.1 : 0.9;
        zoom *= factor;
        zoom = Math.max(0.5, Math.min(3.0, zoom));
        draw();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (currentImage != null) {
            gc.save();
            gc.translate(panX, panY);
            gc.scale(zoom, zoom);
            gc.drawImage(currentImage, 0, 0);
            gc.restore();
        }

        // Рисуем все нарисованные прямоугольники
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        for (Rectangle rect : rectangles) {
            double x = rect.getX1() * zoom + panX;
            double y = rect.getY1() * zoom + panY;
            double width = rect.getWidth() * zoom;
            double height = rect.getHeight() * zoom;
            gc.strokeRect(x, y, width, height);
        }

        if (currentRectangle != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(1);
            gc.setLineDashes(5);

            double x = currentRectangle.getX1() * zoom + panX;
            double y = currentRectangle.getY1() * zoom + panY;
            double width = currentRectangle.getWidth() * zoom;
            double height = currentRectangle.getHeight() * zoom;
            gc.strokeRect(x, y, width, height);

            gc.setLineDashes(0); // Убираем пунктир
        }
    }

    public void loadImage(Image image) {
        this.currentImage = image;
        rectangles.clear();
        marksCount.set(0);
        zoom = 1.0;
        panX = 0;
        panY = 0;
        draw();
    }

    public void clearMarks() {
        rectangles.clear();
        marksCount.set(0);
        draw();
    }

    public List<Rectangle> getRectangles() {
        return new ArrayList<>(rectangles);
    }

    public IntegerProperty getMarksCount() {
        return marksCount;
    }
}
