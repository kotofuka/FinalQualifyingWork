package com.photoMakeup.model;

import org.opencv.core.Rect;

public class CustomRectangle {
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    public CustomRectangle(double x1, double y1, double x2, double y2) {
        this.x1 = (int) Math.min(x1, x2);
        this.y1 = (int) Math.min(y1, y2);
        this.x2 = (int) Math.max(x1, x2);
        this.y2 = (int) Math.max(y1, y2);
    }

    public CustomRectangle(Rect rectangle) {
        this.x1 = rectangle.x;
        this.y1 = rectangle.y;
        this.x2 = this.x1 + rectangle.width;
        this.y2 = this.y1 + rectangle.height;
    }

    public int getX1() {return x1;}
    public int getY1() {return y1;}
    public int getX2() {return x2;}
    public int getY2() {return y2;}

    public int getWidth() {return x2 - x1;}
    public int getHeight() {return y2 - y1;}

    @Override
    public String toString() {
        return String.format("Rectangle[(%d, %d), (%d, %d)]", x1, y1, x2, y2);
    }
}
