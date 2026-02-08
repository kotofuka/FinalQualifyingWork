package com.photoMakeup.service;


import com.photoMakeup.model.Corner;
import com.photoMakeup.model.CornerPoint;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// todo
public class DocumentDetectionService {
    private static Logger logger = LoggerFactory.getLogger(DocumentDetectionService.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public List<CornerPoint> detectDocumentCorners(Mat currentMatImage) {
        logger.info("========== ПОИСК 4 ЧЁРНЫХ ТОЧЕК НА ЛИСТЕ ==========");

        Mat originalImage = currentMatImage.clone();
        int width = originalImage.width();
        int height = originalImage.height();

        logger.info("Размер изображения: {} x {}", width, height);

        List<Point> foundPoints = new ArrayList<>();

        Mat grayImg = new Mat();
        Imgproc.cvtColor(originalImage, grayImg, Imgproc.COLOR_BGR2GRAY);

        // Размытие для шумоподавления
        Imgproc.GaussianBlur(grayImg, grayImg, new Size(5, 5), 0);

        // ИНВЕРТИРОВАННАЯ бинаризация (чёрные точки станут белыми)
        Mat binaryImg = new Mat();
        Imgproc.threshold(grayImg, binaryImg, 0, 255,
                Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY_INV);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
                new Size(3, 3));
        Imgproc.morphologyEx(binaryImg, binaryImg, Imgproc.MORPH_OPEN, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryImg, contours, hierarchy,
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // СОБИРАЕМ ВСЕ НАЙДЕННЫЕ ТОЧКИ
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);

            // Фильтруем по размеру (чёрные точки должны быть меньше, чем белые)
            if (area < 30 || area > 5000) continue;

            // Вычисляем компактность (blob-подобные формы)
            double perimeter = Imgproc.arcLength(
                    new MatOfPoint2f(contour.toArray()), true);
            double circularity = 4 * Math.PI * area / (perimeter * perimeter);

            // Округлые чёрные области (компактность > 0.7)
            if (circularity > 0.7) {
                Moments moments = Imgproc.moments(contour);

                if (moments.m00 > 0) {
                    int cx = (int) (moments.m10 / moments.m00);
                    int cy = (int) (moments.m01 / moments.m00);

                    foundPoints.add(new Point(cx, cy));
                    logger.info("Найдена чёрная точка: (" + cx + ", " + cy +
                            "), компактность=" + String.format("%.2f", circularity) +
                            ", площадь=" + (int)area);
                }
            }
        }

        logger.info("Всего найдено точек: {}", foundPoints.size());

        List<CornerPoint> cornerPoints = findCornerPoints(foundPoints, width, height);

        // Очистка ресурсов
        grayImg.release();
        binaryImg.release();
        kernel.release();
        hierarchy.release();

        return cornerPoints;
    }

    private List<CornerPoint> findCornerPoints(List<Point> allPoints, int width, int height) {
        List<CornerPoint> cornerPoints = new ArrayList<>();

        if (allPoints.size() < 4) {
            logger.warn("Нет найденных 4-х точек для определения углов!");
            return cornerPoints;
        }

        Point topLeft = new Point(0, 0);
        Point topRight = new Point(width, 0);
        Point bottomRight = new Point(width, height);
        Point bottomLeft = new Point(0, height);

        CornerPoint tlCorner = findNearestPoints(allPoints, topLeft, Corner.TOP_LEFT);
        CornerPoint trCorner = findNearestPoints(allPoints, topRight, Corner.TOP_RIGHT);
        CornerPoint btCorner = findNearestPoints(allPoints, bottomLeft, Corner.BOTTOM_LEFT);
        CornerPoint brCorner = findNearestPoints(allPoints, bottomRight, Corner.BOTTOM_RIGHT);

        if (tlCorner != null) cornerPoints.add(tlCorner);
        if (trCorner != null) cornerPoints.add(trCorner);
        if (btCorner != null) cornerPoints.add(btCorner);
        if (brCorner != null) cornerPoints.add(brCorner);

        logger.info("========== ИТОГОВЫЕ УГЛЫ ==========");
        for (CornerPoint cp : cornerPoints) {
            logger.info("{}: точка ({}, {}), расстояние до угла: {}",
                    cp.getCornerType(),
                    cp.getX(),
                    cp.getY());
        }

        return cornerPoints;
    }

    private CornerPoint findNearestPoints(List<Point> points, Point cornerPoint, Corner corner) {
        if (points.isEmpty()) return null;

        Point nearestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Point p : points) {
            double distance = distance(p, cornerPoint);

            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = p;
            }
        }

        if (nearestPoint == null) {
            logger.debug("Для угла {}: ближайшая точка ({}, {}), расстояние {}",
                    corner.toString(),
                    nearestPoint.x,
                    nearestPoint.y,
                    String.format("%.2f", minDistance)
            );
        }
        return new CornerPoint(nearestPoint.x, nearestPoint.y, corner);
    }

    private double distance(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

}
