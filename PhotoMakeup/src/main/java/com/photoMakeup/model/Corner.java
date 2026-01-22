package com.photoMakeup.model;

public enum Corner {
    TOP_LEFT("Верхний левый угол"),
    TOP_RIGHT("Верхний правый угол"),
    BOTTOM_LEFT("Нижний левый угол"),
    BOTTOM_RIGHT("Нижний правый угол");

    private final String description;

    Corner(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
