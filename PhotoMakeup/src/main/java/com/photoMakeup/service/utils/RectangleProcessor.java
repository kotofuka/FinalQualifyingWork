package com.photoMakeup.service.utils;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

@FunctionalInterface
public interface RectangleProcessor {
    void process(Mat image, Rect rectangle);
}
