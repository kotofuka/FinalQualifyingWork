package com.photoMakeup.service;

import com.photoMakeup.model.Corner;
import com.photoMakeup.model.CornerPoint;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PerspectiveTransformService {
    private static final Logger logger = LoggerFactory.getLogger(PerspectiveTransformService.class);

    static {
        try {
            logger.info("🔄 Загружаю OpenCV...");
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            logger.info("✅ OpenCV загружена!");
        } catch (UnsatisfiedLinkError e) {
            logger.error("❌ ОШИБКА загрузки OpenCV", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * ГЛАВНЫЙ МЕТОД: Нормализует и обрезает изображение
     * <p>
     * ШАГ 1: Применяем перспективное преобразование
     * ШАГ 2: Выпрямляем наклонное изображение
     * ШАГ 3: Обрезаем только прямоугольник внутри чёрных точек
     */
    public Mat perspectiveTransform(Mat sourceImage, List<CornerPoint> corners) {
        if (corners == null || corners.size() != 4) {
            logger.error("❌ Требуется ровно 4 угла, получено: {}", corners == null ? 0 : corners.size());
            return sourceImage.clone();
        }

        try {
            logger.info("========== НАЧАЛО НОРМАЛИЗАЦИИ И ОБРЕЗКИ ==========");

            // ШАГ 1: Получаем координаты чёрных точек
            logger.info("ШАГ 1: Получаю координаты чёрных точек...");
            Point[] srcPoints = new Point[4];

            for (int i = 0; i < corners.size(); i++) {
                srcPoints[i] = new Point(corners.get(i).getX(), corners.get(i).getY());
            }

            logger.info("✅ КООРДИНАТЫ ЧЁРНЫХ ТОЧЕК:");
            logger.info("   TOP_LEFT:     ({}, {})", (int) srcPoints[0].x, (int) srcPoints[0].y);
            logger.info("   TOP_RIGHT:    ({}, {})", (int) srcPoints[1].x, (int) srcPoints[1].y);
            logger.info("   BOTTOM_LEFT:  ({}, {})", (int) srcPoints[2].x, (int) srcPoints[2].y);
            logger.info("   BOTTOM_RIGHT: ({}, {})", (int) srcPoints[3].x, (int) srcPoints[3].y);

            // ШАГ 2: Вычисляем размеры прямоугольника
            logger.info("ШАГ 2: Вычисляю размеры выходного прямоугольника...");

            // Расстояние между левой-верхней и правой-верхней точками
            double topWidth = distance(srcPoints[0], srcPoints[1]);

            // Расстояние между левой-нижней и правой-нижней точками
            double bottomWidth = distance(srcPoints[2], srcPoints[3]);

            // Расстояние между левой-верхней и левой-нижней точками
            double leftHeight = distance(srcPoints[0], srcPoints[2]);

            // Расстояние между правой-верхней и правой-нижней точками
            double rightHeight = distance(srcPoints[1], srcPoints[3]);

            // Берём максимальные значения для точности
            double width = Math.max(topWidth, bottomWidth);
            double height = Math.max(leftHeight, rightHeight);

            logger.info("📏 РАЗМЕРЫ ВЫХОДНОГО ИЗОБРАЖЕНИЯ:");
            logger.info("   Ширина:  {} px", (int) width);
            logger.info("   Высота:  {} px", (int) height);

            // ШАГ 3: Создаём целевые точки (выпрямленный прямоугольник)
            logger.info("ШАГ 3: Создаю целевой прямоугольник (0, 0) до ({}, {})...", (int) width, (int) height);

            Point[] dstPoints = new Point[4];
            dstPoints[0] = new Point(0, 0);                    // TOP_LEFT
            dstPoints[1] = new Point(width - 1, 0);           // TOP_RIGHT
            dstPoints[2] = new Point(0, height - 1);          // BOTTOM_LEFT
            dstPoints[3] = new Point(width - 1, height - 1);  // BOTTOM_RIGHT

            // ШАГ 4: Вычисляем матрицу перспективного преобразования
            logger.info("ШАГ 4: Вычисляю матрицу преобразования...");
            Mat perspectiveMatrix = Imgproc.getPerspectiveTransform(
                    new MatOfPoint2f(srcPoints),
                    new MatOfPoint2f(dstPoints)
            );

            // ШАГ 5: Применяем преобразование
            logger.info("ШАГ 5: Применяю warpPerspective...");
            Mat transformed = new Mat();
            Imgproc.warpPerspective(
                    sourceImage,                    // Входное изображение
                    transformed,                    // Выходное изображение
                    perspectiveMatrix,              // Матрица преобразования
                    new Size(width, height),       // Размер выхода
                    Imgproc.INTER_LINEAR,          // Качество интерполяции
                    Core.BORDER_REPLICATE,         // Повторяем краевые пиксели
                    new Scalar(255, 255, 255)      // Белый фон на случай
            );

            logger.info("✅ НОРМАЛИЗАЦИЯ ЗАВЕРШЕНА!");
            logger.info("   Результат:");
            logger.info("   ✓ Изображение выпрямлено");
            logger.info("   ✓ Размер: {} x {} px", (int) width, (int) height);
            logger.info("   ✓ Чёрные точки по краям: (0,0), ({},0), (0,{}), ({},{})",
                    (int) (width - 1), (int) (height - 1), (int) (width - 1), (int) (height - 1));
            logger.info("   ✓ Всё содержимое внутри сохранено");

            return transformed;

        } catch (Exception e) {
            logger.error("❌ ОШИБКА при преобразовании", e);
            e.printStackTrace();
            return sourceImage.clone();
        }
    }

    private int getCornerIndex(Corner corner) {
        switch (corner) {
            case TOP_LEFT: return 0;
            case TOP_RIGHT: return 1;
            case BOTTOM_LEFT: return 2;
            case BOTTOM_RIGHT: return 3;
            default: throw new IllegalArgumentException("Неизвестный угол: " + corner);
        }
    }

    /**
     * Вспомогательный метод: Расстояние между двумя точками
     */
    private double distance(Point p1, Point p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}