package com.photoMakeup.service.utils;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class GrayScaleProcessor implements RectangleProcessor{
    @Override
    public void process(Mat image, Rect rectangle){
        Mat roi = image.submat(rectangle);
        Mat gray = new Mat();
        Mat grayBgr = new Mat();
        try {
            Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(gray, grayBgr, Imgproc.COLOR_GRAY2BGR);
            grayBgr.copyTo(roi);
        } finally {
            gray.release();
            grayBgr.release();
        }
    }
}
