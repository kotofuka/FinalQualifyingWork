package com.photoMakeup.service.utils;

import com.photoMakeup.model.CustomRectangle;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public interface GrayScaleImp extends ConverterImp{

//    default void grayScale(CanvasPanel canvasPanel){
//        var image = canvasPanel.getViewModel().getCurrentImage();
//
//        double width = image.getWidth();
//        double height = image.getHeight();
//
//        WritableImage wr = new WritableImage(image.getPixelReader(), (int) width, (int) height);
//
//        var pixelReader = wr.getPixelReader();
//        var pixelWriter = wr.getPixelWriter();
//
//        for(CustomRectangle rectangle: canvasPanel.getViewModel().getRectangles()){
//            for (int x = rectangle.getX1(); x < rectangle.getX2(); x++) {
//                for (int y = rectangle.getY1(); y < rectangle.getY2(); y++) {
//
//
//                    Color color = pixelReader.getColor(x, y);
//
//                    int gray = (int) Math.round(
//                            0.299 * color.getRed() * 255 +
//                                    0.587 * color.getGreen() * 255 +
//                                    0.114 * color.getBlue() * 255
//                    );
//                    gray = Math.max(0, Math.min(255, gray));
//
//                    pixelWriter.setColor(x, y, Color.grayRgb(gray));
//                }
//            }
//        }
//
//        canvasPanel.getViewModel().setCurrentImage(wr);
//        canvasPanel.redraw();
//    }

    default void grayScale(CanvasPanel canvasPanel){
        Mat image = imageToMat(canvasPanel.getViewModel().getCurrentImage());
        if (image.empty()){
            throw new IllegalArgumentException("Image is empty");
        }

        try {


            List<Rect> rectangles = canvasPanel.getViewModel().getRectangles().stream().map((x) -> customRectToRect(x)).toList();

            Rect imageBounds = new Rect(0, 0, image.width(), image.height());

            for (Rect rectangle : rectangles) {
                Mat roi = image.submat(rectangle);
                Mat gray = new Mat();
                Mat grayBgr = new Mat();

                try {
                    Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.cvtColor(gray, grayBgr, Imgproc.COLOR_GRAY2BGR);
                    grayBgr.copyTo(roi);
                } finally {
                    roi.release();
                    gray.release();
                    grayBgr.release();
                }
            }
            canvasPanel.setCurrentImage(matToImage(image));
            canvasPanel.redraw();
        } finally {
            image.release();
        }
    }
}
