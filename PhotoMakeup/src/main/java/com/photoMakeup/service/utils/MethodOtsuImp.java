package com.photoMakeup.service.utils;

import com.photoMakeup.model.CustomRectangle;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;


public interface MethodOtsuImp extends GrayScaleImp {
    default void methodOtsu(CanvasPanel canvasPanel) {
        grayScale(canvasPanel);

        var image = canvasPanel.getViewModel().getCurrentImage();

        double width = image.getWidth();
        double height = image.getHeight();

        WritableImage wr = new WritableImage(image.getPixelReader(), (int) width, (int) height);

        var pixelReader = wr.getPixelReader();
        var pixelWriter = wr.getPixelWriter();

        var rectangles = canvasPanel.getViewModel().getRectangles();

        int threshold = findOptimalThreshold(pixelReader, rectangles);

        applyThreshold(pixelReader, pixelWriter, threshold, rectangles);

        canvasPanel.getViewModel().setCurrentImage(wr);
        canvasPanel.redraw();
    }

    private int findOptimalThreshold(PixelReader pixelReader, List<CustomRectangle> rectangles) {
        int[] histogram = new int[256];
        int totalPixels = 0;
        for (var rectangle : rectangles) {
            totalPixels += rectangle.getWidth() * rectangle.getHeight();
            for (int y = rectangle.getY1(); y < rectangle.getY2(); y++) {
                for (int x = rectangle.getX1(); x < rectangle.getX2(); x++) {
                    histogram[(int)(pixelReader.getColor(x, y).getBlue() * 255)]++;
                }
            }
        }

        double sum = 0;
        for(int i = 0 ; i < 256 ; i++){
            sum += i * histogram[i];
        }

        double sumB = 0;      // Сумма интенсивностей для фона
        int wB = 0;           // Количество пикселей фона
        int wF = 0;           // Количество пикселей объекта
        double maxVariance = 0; // Максимальная межклассовая дисперсия
        int threshold = 0;    // Оптимальный порог

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];               // Вес класса фона
            if (wB == 0) continue;

            wF = totalPixels - wB;            // Вес класса объекта
            if (wF == 0) break;

            sumB += t * histogram[t];         // Сумма интенсивностей фона

            double mB = sumB / wB;            // Среднее фона
            double mF = (sum - sumB) / wF;    // Среднее объекта

            // Межклассовая дисперсия: σ² = ω₀ * ω₁ * (μ₀ - μ₁)²
            double varianceBetween =
                    (double) wB / totalPixels *
                            (double) wF / totalPixels *
                            Math.pow(mB - mF, 2);

            // Поиск максимума дисперсии
            if (varianceBetween > maxVariance) {
                maxVariance = varianceBetween;
                threshold = t;
            }
        }

        return threshold;
    }

    private void applyThreshold(PixelReader pixelReader, PixelWriter pixelWriter, int threshold,
                                List<CustomRectangle> rectangles) {
        for (var rectangle : rectangles) {
            for (int y = rectangle.getY1(); y < rectangle.getY2(); y++) {
                for (int x = rectangle.getX1(); x < rectangle.getX2(); x++) {
                    if ((int)(pixelReader.getColor(x, y).getBlue() * 255) > threshold) {
                        pixelWriter.setColor(x, y, Color.WHITE);
                    } else {
                        pixelWriter.setColor(x, y, Color.BLACK);
                    }
                }
            }
        }
    }
}
