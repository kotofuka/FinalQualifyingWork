package com.photoMakeup.service.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.photoMakeup.model.CustomRectangle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface FileFormatter {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    default void saveMarks(List<CustomRectangle> rectangles, File file) throws IOException{
        List<Map<String, Object>> data = new ArrayList<>();

        for (CustomRectangle rectangle : rectangles) {
            Map<String, Object> rectangleMap = Map.of(
                    "point1", Map.of("x", rectangle.getX1(), "y", rectangle.getY1()),
                    "point2", Map.of("x", rectangle.getX2(), "y", rectangle.getY2())
            );
            data.add(rectangleMap);
        }
        Map<String, Object> result = Map.of(
                "marks", data,
                "count", rectangles.size()
        );

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(result, writer);
        }
    }

    default List<CustomRectangle> loadMarks(File file) throws IOException{
        List<CustomRectangle> rectangles = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> data = gson.fromJson(reader, Map.class);
            List<Map<String, Object>> marks = (List<Map<String, Object>>) data.get("marks");

            if (marks != null) {
                for (Map<String, Object> mark : marks) {
                    Map<String, Number> point1 = (Map<String, Number>) mark.get("point1");
                    Map<String, Number> point2 = (Map<String, Number>) mark.get("point2");

                    double x1 = point1.get("x").doubleValue();
                    double y1 = point1.get("y").doubleValue();
                    double x2 = point2.get("x").doubleValue();
                    double y2 = point2.get("y").doubleValue();

                    rectangles.add(new CustomRectangle(x1, y1, x2, y2));
                }
            }
        }
        return rectangles;
    }
}
