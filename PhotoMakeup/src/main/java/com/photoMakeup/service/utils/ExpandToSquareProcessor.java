package com.photoMakeup.service.utils;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ExpandToSquareProcessor implements RectangleProcessor{
    private final int COLOR_THRESHOLD = 20;     // Порог изменения цвета
    private final int MIN_EDGE_LENGTH = 8;      // Минимальная длина подверженности границы
    private final double EDGE_CONFIDENCE = 0.7; // Минимальная уверенность границы


    @Override
    public void process(Mat image, Rect rectangle) {
        if (rectangle.width <= 0 || rectangle.height <= 0) {
            throw new IllegalArgumentException("rectangle width and height must be greater than 0");
        }

        Mat gray = new Mat();
        if (image.channels() > 1){
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            image.copyTo(gray);
        }

        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0.5);

        double innerAverage = calculateAverage(gray, rectangle);

        Rect expanded = expandRectangle(gray, rectangle, innerAverage);

        rectangle.x = expanded.x;
        rectangle.y = expanded.y;
        rectangle.width = expanded.width;
        rectangle.height = expanded.height;

        gray.release();
    }

    private Rect expandRectangle(Mat gray, Rect init_rectangle, double innerAverage){
        int left = init_rectangle.x;
        int right = init_rectangle.x + init_rectangle.width;
        int top = init_rectangle.y;
        int bottom = init_rectangle.y + init_rectangle.height;

        while (left > 0 && !isVerticalEdge(gray, left - 1, top, bottom - top + 1, innerAverage)){
            left--;
        }

        while (right < gray.cols() && !isVerticalEdge(gray,right, top, bottom - top + 1, innerAverage)){
            right++;
        }

        while (top > 0 && !isHorizontalEdge(gray, left, top - 1, right - left + 1, innerAverage)){
            top--;
        }

        while (bottom < gray.rows() && !isHorizontalEdge(gray, left, bottom, right - left + 1, innerAverage)){
            bottom++;
        }

        if (left >= right || top >= bottom) return init_rectangle;
        return new Rect(left, top, right - left, bottom- top);
    }

    private boolean isVerticalEdge(Mat gray, int x, int yStart, int height, double average){
        int edgePixels = 0;
        int total = 0;

        for (int y = yStart; y < yStart + height && y < gray.rows(); y++){
            if (x < 0 || x >= gray.cols()) continue;

            double pixel = gray.get(y, x)[0];
            double diff = Math.abs(pixel - average);
            if (diff > COLOR_THRESHOLD){
                edgePixels++;
            }
            total++;
        }
        return total > 0 && ((double) edgePixels / total) >= EDGE_CONFIDENCE && edgePixels >= MIN_EDGE_LENGTH;
    }

    private boolean isHorizontalEdge(Mat gray, int xStart, int y, int width, double average){
        int edgePixels = 0;
        int total = 0;

        for (int x = xStart; x < xStart + width && x < gray.cols(); x++){
            if (y < 0 || y >= gray.rows()) continue;

            double pixel = gray.get(y, x)[0];
            double diff = Math.abs(pixel - average);

            if (diff > COLOR_THRESHOLD){
                edgePixels++;
            }
            total++;
        }
        return total > 0 && (((double) edgePixels / total) >= EDGE_CONFIDENCE) && edgePixels >= MIN_EDGE_LENGTH;
    }

    private double calculateAverage(Mat gray, Rect rectangle){
        double sum = 0;
        int count = 0;

        for (int y = rectangle.y; y < rectangle.y + rectangle.height && y < gray.rows(); y++){
            for (int x = rectangle.x; x < rectangle.x + rectangle.width && x < gray.cols(); x++){
                sum += gray.get(y, x)[0];
                count++;
            }
        }
        return count > 0 ? sum / count : 128.0;
    }
}
