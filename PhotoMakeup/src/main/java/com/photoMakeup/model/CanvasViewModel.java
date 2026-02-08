package com.photoMakeup.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class CanvasViewModel {
    private Image currentImage;
    private double zoom = 1.0;
    private double panX = 0, panY = 0;

    private boolean isShowRectangles = true;
    private List<CustomRectangle> rectangles = new ArrayList<>();
    private CustomRectangle currentRectangle;
    private double startX, startY;

    private boolean isPanning = false;
    private double panStartX, panStartY;

    private final IntegerProperty marksCount = new SimpleIntegerProperty(0);
    private final ObjectProperty<double[]> mouseCoordinates = new SimpleObjectProperty<>(null);

    public CanvasViewModel() {}

    public Image getCurrentImage() {return currentImage;}

    public void setCurrentImage(Image currentImage){
        setCurrentImage(currentImage, false);
    }

    public void setCurrentImage(Image currentImage, boolean isNewFile) {
        this.currentImage = currentImage;
        if (isNewFile) this.clearRectangles();
    }

    public double getCurrentImageWidth() {return currentImage != null? currentImage.getWidth(): 0;}
    public double getCurrentImageHeight() {return currentImage != null? currentImage.getHeight(): 0;}

    public double getZoom() {return zoom;}
    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public boolean isShowRectangles() {return isShowRectangles;}

    public void setShowRectangles(boolean isShowRectangles) {
        this.isShowRectangles = isShowRectangles;
    }

    public double getPanX() {
        return panX;
    }

    public void setPan(double x, double y) {
        this.panX = x;
        this.panY = y;
    }

    public double getPanY() {
        return panY;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStart(double x, double y) {
        this.startX = x;
        this.startY = y;
    }

    public boolean isPanning() {
        return isPanning;
    }

    public void setPanning(boolean panning) {
        isPanning = panning;
    }

    public double getPanStartX() {
        return panStartX;
    }

    public void setPanStart(double x, double y) {
        this.panStartX = x;
        this.panStartY = y;
    }

    public double getPanStartY() {
        return panStartY;
    }

    public List<CustomRectangle> getRectangles() {return new ArrayList<>(rectangles);}

    public void setRectangles(List<CustomRectangle> rectangles) {
        this.rectangles = new ArrayList<>(rectangles);
        isShowRectangles = true;
        marksCount.set(rectangles.size());
    }

    public CustomRectangle getCurrentRectangle() {
        return currentRectangle;
    }

    public void setCurrentRectangle(CustomRectangle currentRectangle) {
        this.currentRectangle = currentRectangle;
    }

    public IntegerProperty marksCountProperty() {return marksCount;}
    public ObjectProperty<double[]> mouseCoordinatesProperty() {return mouseCoordinates;}

    public void addRectangle(CustomRectangle rectangle) {
        rectangles.add(rectangle);
    }

    public void removeRectangle(CustomRectangle rectangle) {
        rectangles.remove(rectangle);
    }

    public void clearRectangles() {
        rectangles.clear();
        isShowRectangles = true;
        currentRectangle = null;
        marksCountProperty().set(0);
        zoom = 1.0;
        panX = 0;
        panY = 0;
    }
}
