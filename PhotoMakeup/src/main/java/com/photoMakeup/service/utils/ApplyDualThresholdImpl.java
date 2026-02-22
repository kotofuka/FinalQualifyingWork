package com.photoMakeup.service.utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public interface ApplyDualThresholdImpl {
    default Mat applyDualThreshold(Mat gray32f, Mat thresholdMap, int minThreshold, int maxThreshold) {
        Mat gray8u = new Mat();
        Mat binary = new Mat();
        try{
            gray32f.convertTo(gray8u, CvType.CV_8U);

            Mat minMask = new Mat();
            Mat maxMask = new Mat();
            try{
                Core.compare(gray8u, new Scalar(minThreshold), minMask, Core.CMP_LT);
                Core.compare(gray8u, new Scalar(maxThreshold), maxMask, Core.CMP_GT);

                Mat adaptiveBinary = new Mat();
                try{
                    Core.compare(gray32f, thresholdMap, adaptiveBinary, Core.CMP_GT);
                    adaptiveBinary.convertTo(adaptiveBinary, CvType.CV_8U, 255);

                    adaptiveBinary.copyTo(binary);

                    Mat black = Mat.zeros(adaptiveBinary.size(), CvType.CV_8U);
                    try {
                        black.copyTo(binary, minMask);
                    } finally {
                        black.release();
                    }

                    Mat white = Mat.ones(adaptiveBinary.size(), CvType.CV_8U);
                    try {
                        white.convertTo(white, CvType.CV_8U, 255);
                        white.copyTo(binary, maxMask);
                    } finally {
                        white.release();
                    }
                } finally {
                    adaptiveBinary.release();
                }
            } finally {
                minMask.release();
                maxMask.release();
            }
        } finally {
            gray8u.release();
        }
        return binary;
    }
}
