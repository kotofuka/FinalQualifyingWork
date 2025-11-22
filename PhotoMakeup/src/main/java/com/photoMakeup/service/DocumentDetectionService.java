package com.photoMakeup.service;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO нужно доделать файл
public class DocumentDetectionService {
    public static final Logger logger = LoggerFactory.getLogger(DocumentDetectionService.class);

    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }
}
