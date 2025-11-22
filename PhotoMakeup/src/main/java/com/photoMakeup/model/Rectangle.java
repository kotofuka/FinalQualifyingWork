package com.photoMakeup.model;

public class Rectangle {
    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public Rectangle(double x1, double y1, double x2, double y2) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
    }

    public double getX1() {return this.x1;}
    public double getY1() {return this.y1;}
    public double getX2() {return this.x2;}
    public double getY2() {return this.y2;}

    public double getWidth() {return this.x2 - this.x1;}
    public double getHeight() {return this.y2 - this.y1;}

    @Override
    public String toString() {
        return String.format("Rectangle[(%f, %f), (%f, %f)]", x1, y1, x2, y2);
    }
}
