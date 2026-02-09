package com.photoMakeup.service;

import com.photoMakeup.service.utils.ConverterImp;
import com.photoMakeup.service.utils.NiblackMethodProcessor;
import com.photoMakeup.service.utils.RectangleProcessor;
import com.photoMakeup.ui.CanvasPanel;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

public class BinarizationService implements ConverterImp {
    private CanvasPanel canvasPanel;

    public BinarizationService(CanvasPanel canvasPanel) {
        this.canvasPanel = canvasPanel;
    }

    public void doAction(RectangleProcessor func){
        Mat image = imageToMat(canvasPanel.getViewModel().getCurrentImage());
        if (image.empty()){
            throw new IllegalArgumentException("Image is empty");
        }

        try {
            List<Rect> rectangles = canvasPanel.getViewModel().getRectangles().stream().map((x) -> customRectToRect(x)).toList();

            for (Rect rectangle : rectangles) {
                func.process(image, rectangle);
            }
            canvasPanel.setCurrentImage(matToImage(image));
            canvasPanel.redraw();
        } finally {
            image.release();
        }
    }
}
