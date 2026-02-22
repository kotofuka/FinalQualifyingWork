package com.photoMakeup.service.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager<T> {
    private final Deque<T> history;
    private final int maxSize;

    public UndoManager(int maxSize) {
        this.maxSize = maxSize;
        history = new ArrayDeque<>(maxSize);
    }

    public void push(T item) {
        if (history.size() >= maxSize) {
            history.removeFirst();
        }
        history.addLast(item);
    }

    public T undo(){
        return history.pollLast();
    }

    public int size(){
        return history.size();
    }

    public void clear(){
        history.clear();
    }
}
