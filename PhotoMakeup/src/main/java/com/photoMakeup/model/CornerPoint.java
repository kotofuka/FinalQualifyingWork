package com.photoMakeup.model;

public class CornerPoint {
    private double x;
    private double y;
    private Corner cornerType;  // "TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"

    public CornerPoint(double x, double y, Corner cornerType) {
        this.x = x;
        this.y = y;
        this.cornerType = cornerType;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Corner getCornerType() { return cornerType; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        return String.format("📍 %s: (%.0f, %.0f)", cornerType, x, y);
    }
}