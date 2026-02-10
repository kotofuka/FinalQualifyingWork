package com.photoMakeup.service.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class MethodKMeansProcessor implements RectangleProcessor{
    private final int k = 2;
    private final int attempts;
    private final int maxIterations;

    public MethodKMeansProcessor(int attempts, int maxIterations) {
        this.attempts = attempts;
        this.maxIterations = maxIterations;
    }

    @Override
    public void process(Mat image, Rect rectangle) {
        Mat roi = image.submat(rectangle);
        Mat gray = new Mat();
        Mat samples = new Mat();
        Mat labels = new Mat();
        Mat centers = new Mat();

        try{
            if (roi.channels() > 1){
                Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                gray = roi.clone();
            }

            int totalPixels = gray.rows() * gray.cols();
            samples.create(totalPixels, 1, CvType.CV_32F);

            int idx = 0;
            for (int y = 0; y < gray.rows(); y++){
                for (int x = 0; x < gray.cols(); x++){
                    double[] pixel = gray.get(y, x);
                    samples.put(idx++, 0, (float) pixel[0]);
                }
            }

            Core.kmeans(
                    samples,
                    k,
                    labels,
                    new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, maxIterations, 1.0),
                    attempts,
                    Core.KMEANS_PP_CENTERS,
                    centers
            );

            double[] centerValues = new double[k];
            for (int i = 0; i < k; i++){
                centerValues[i] = centers.get(i, 0)[0];
            }

            int darkestCluster = 0;
            int brightestCluster = 0;
            for (int i = 0; i < k; i++){
                if (centerValues[i] < centerValues[darkestCluster]){
                    darkestCluster = i;
                }
                if (centerValues[i] > centerValues[brightestCluster]){
                    brightestCluster = i;
                }
            }

            Mat binary = new Mat(gray.size(), CvType.CV_8UC1);
            try {
                idx = 0;
                for (int y = 0; y < gray.rows(); y++) {
                    for (int x = 0; x < gray.cols(); x++) {
                        int cluster = (int) labels.get(idx++, 0)[0];
                        int value = cluster == darkestCluster ? 0 : 255;
                        binary.put(y, x, value);
                    }
                }
                if (image.channels() > 1) {
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
                binary.release();
            }
        } finally {
            if (gray != roi) gray.release();
            samples.release();
            labels.release();
            centers.release();
        }
    }
}
