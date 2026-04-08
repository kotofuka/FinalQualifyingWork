package com.photoMakeup.service.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class SauvolaMethodProcessor implements RectangleProcessor, ApplyDualThresholdImpl{
    private final double k;
    private final double R;
    private final int windowSize;
    private final int minThreshold;
    private final int maxThreshold;


    public SauvolaMethodProcessor(double k, double R, int windowSize, int minThreshold, int maxThreshold){
        this.k = k;
        this.R = R;
        this.windowSize = (windowSize % 2 == 0) ? windowSize + 1 : windowSize;

        this.minThreshold = Math.max(0, Math.min(minThreshold, 255));
        this.maxThreshold = Math.max(0, Math.min(maxThreshold, 255));
    }

    @Override
    public void process(Mat image, Rect rectangle) {
        Mat roi = image.submat(rectangle);
        Mat gray32f = new Mat();

        try{
            if (roi.channels() > 1){
                Mat gray8u = new Mat();
                try{
                    Imgproc.cvtColor(roi, gray8u, Imgproc.COLOR_BGR2GRAY);
                    gray8u.convertTo(gray32f, CvType.CV_32F);
                } finally {
                    gray8u.release();
                }
            } else {
                roi.convertTo(gray32f, CvType.CV_32F);
            }

            Mat mean = new Mat();
            Mat graySquared = new Mat();
            Mat meanSquared = new Mat();
            Mat meanSquaredValue = new Mat();
            Mat variance = new Mat();
            Mat stddev = new Mat();
            Mat normalizedStddev = new Mat();
            Mat factor = new Mat();
            Mat thresholdMap = new Mat();
            Mat binary = new Mat();
            try{
                Imgproc.boxFilter(gray32f, mean, CvType.CV_32F,
                        new Size(windowSize, windowSize),
                        new Point(-1, -1),
                        true,
                        Core.BORDER_REPLICATE
                );

                Core.multiply(gray32f, gray32f, graySquared);
                Imgproc.boxFilter(graySquared, meanSquared, CvType.CV_32F,
                        new Size(windowSize, windowSize),
                        new Point(-1, -1),
                        true,
                        Core.BORDER_REPLICATE
                );

                Core.multiply(mean, mean, meanSquaredValue);
                Core.subtract(meanSquared, meanSquaredValue, variance);
                Core.max(variance, new Scalar(0), variance);

                Core.sqrt(variance, stddev);
                Core.divide(stddev, new Scalar(R), normalizedStddev);

                Core.subtract(normalizedStddev, new Scalar(1.0), factor);
                Core.multiply(factor, new Scalar(k), factor);
                Core.add(factor, new Scalar(1.0), factor);

                Core.multiply(mean, factor, thresholdMap);

                binary = applyDualThreshold(gray32f, thresholdMap, minThreshold, maxThreshold);

                if (image.channels() > 1){
                    Mat binaryBgr = new Mat();
                    try {
                        Imgproc.cvtColor(binary, binaryBgr, Imgproc.COLOR_GRAY2BGR);
                        binaryBgr.copyTo(roi);
                    } finally {
                        binaryBgr.release();
                    }
                } else {
                    binary.copyTo(roi);
                }

            } finally {
                mean.release();
                graySquared.release();
                meanSquared.release();
                meanSquaredValue.release();
                variance.release();
                stddev.release();
                normalizedStddev.release();
                factor.release();
                thresholdMap.release();
                binary.release();
            }

        } finally {
            gray32f.release();
        }
    }
}
