package com.photoMakeup.service;

import com.photoMakeup.model.CanvasViewModel;
import com.photoMakeup.model.CustomRectangle;

public class ImageAnnotationService {
    private final CanvasViewModel viewModel;

    private final CanvasRenderingService renderingService;

    public ImageAnnotationService(CanvasViewModel viewModel, CanvasRenderingService renderingService) {
        this.viewModel = viewModel;
        this.renderingService = renderingService;
    }

    public void deleteRectangleAt(double x, double y) {
        var zoom = viewModel.getZoom();
        for (CustomRectangle rectangle: viewModel.getRectangles()){
            double screenX1 = rectangle.getX1() * zoom + viewModel.getPanX();
            double screenY1 = rectangle.getY1() * zoom + viewModel.getPanY();
            double screenX2 = rectangle.getX2() * zoom + viewModel.getPanX();
            double screenY2 = rectangle.getY2() * zoom + viewModel.getPanY();

            if (x >= screenX1 && x <= screenX2 && y >= screenY1 && y <= screenY2){
                viewModel.removeRectangle(rectangle);
                viewModel.marksCountProperty().set(viewModel.getRectangles().size());
                renderingService.render();
                return;
            }
        }
    }
}
