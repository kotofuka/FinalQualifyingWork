package com.photoMakeup.service.utils;

import com.photoMakeup.model.CustomRectangle;
import com.photoMakeup.ui.CanvasPanel;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.List;


public class MethodOtsuProcessor implements RectangleProcessor {

    @Override
    public void process(Mat image, Rect rectangle) {
        Mat roi = image.submat(rectangle);
        Mat gray = new Mat();
        Mat binary = new Mat();

        try{
            if (image.channels() == 3 || image.channels() == 4) {
                Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                roi.copyTo(gray);
            }

            double threshold = Imgproc.threshold(
                    gray,
                    binary,
                    0,
                    255,
                    Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU
            );

            if (image.channels() == 1){
                binary.copyTo(roi);
            } else {
                Mat binaryBgr = new Mat();
                try {
                    Imgproc.cvtColor(binary, binaryBgr, Imgproc.COLOR_GRAY2BGR);
                    binaryBgr.copyTo(roi);
                } finally {
                    binaryBgr.release();
                }
            }
        } finally {
            gray.release();
            binary.release();
        }
    }
}
