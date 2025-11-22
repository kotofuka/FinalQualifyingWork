package com.photoMakeup.model;

public class CornerPoint {
    private double x;
    private double y;
    private Corner corner;

    public CornerPoint(double x, double y, Corner corner) {
        this.x = x;
        this.y = y;
        this.corner = corner;
    }

    public double getX() {return x;}
    public void setX(double x) {this.x = x;}

    public double getY() {return y;}
    public void setY(double y) {this.y = y;}

    public Corner getCorner() {return corner;}

    @Override
    public String toString() {
        return corner.getDescription() + " (" + (int)x + ", " + (int)y + ")";
    }

}
