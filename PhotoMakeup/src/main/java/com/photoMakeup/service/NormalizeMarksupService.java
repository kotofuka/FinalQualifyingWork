package com.photoMakeup.service;

import com.photoMakeup.model.CustomRectangle;
import com.photoMakeup.service.utils.ConverterImp;
import com.photoMakeup.service.utils.RectangleProcessor;
import com.photoMakeup.ui.CanvasPanel;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class NormalizeMarksupService implements ConverterImp {
    private final CanvasPanel canvasPanel;

    public NormalizeMarksupService(CanvasPanel canvasPanel) {
        this.canvasPanel = canvasPanel;
    }

    public void doActionWithRectangle(RectangleProcessor func){
        Mat image = imageToMat(canvasPanel.getViewModel().getCurrentImage());
        if (image.empty()){
            throw new IllegalArgumentException("Image is empty");
        }

        try {
            List<Rect> rectangles = canvasPanel.getViewModel().getRectangles().stream().map(this::customRectToRect).toList();
            List<CustomRectangle> updatedRectangles = new ArrayList<>(rectangles.size());

            for (Rect rectangle : rectangles) {
                func.process(image, rectangle);
                updatedRectangles.add(new CustomRectangle(rectangle));
            }
            canvasPanel.getViewModel().setRectangles(updatedRectangles);
            canvasPanel.setCurrentImage(matToImage(image));
            canvasPanel.redraw();
        } finally {
            image.release();
        }
    }

}
