package com.photoMakeup.model;

public class CornerPoint {
    private double x;
    private double y;
    private Corner type;

    public CornerPoint(double x, double y, Corner type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public double getX() {return x;}
    public double getY() {return y;}
    public Corner getType() {return type;}

    public void setX(double x) {this.x = x;}
    public void setY(double y) {this.y = y;}

    @Override
    public String toString() {
        return String.format("CornerPoint: %s (x=%.2f, y=%.2f)", type, x, y);
    }
}
