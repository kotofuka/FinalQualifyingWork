package com.photoMakeup.service;

import com.photoMakeup.model.CanvasViewModel;
import com.photoMakeup.service.utils.UndoManager;
import com.photoMakeup.ui.CanvasPanel;

public class UndoService {
    private final UndoManager undoManager;
    private final CanvasPanel canvasPanel;

    public UndoService(int sizeMax, CanvasPanel canvasPanel) {
        this.undoManager = new UndoManager(sizeMax);
        this.canvasPanel = canvasPanel;
    }

    public void undo() {
        var data = undoManager.undo();


    }

    public void clear(){
        undoManager.clear();
    }

    public void update(){
        // find all data
        CanvasViewModel viewModel = canvasPanel.getViewModel().copy();

    }
}
