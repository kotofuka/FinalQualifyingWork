package com.photoMakeup.model;

public class CustomRectangle {
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public CustomRectangle(double x1, double y1, double x2, double y2) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
    }

    public double getX1() {return x1;}
    public double getY1() {return y1;}
    public double getX2() {return x2;}
    public double getY2() {return y2;}

    public double getWidth() {return x2 - x1;}
    public double getHeight() {return y2 - y1;}

    @Override
    public String toString() {
        return String.format("Rectangle[(%f, %f), (%f, %f)]", x1, y1, x2, y2);
    }
}
