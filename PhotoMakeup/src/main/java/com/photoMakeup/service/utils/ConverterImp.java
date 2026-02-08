package com.photoMakeup.service.utils;

import com.photoMakeup.model.CustomRectangle;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;

public interface ConverterImp {
    default Mat imageToMat(Image image) {
        if (image == null) {
            return null;
        }
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        if (bufferedImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage converted = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR
            );
            converted.getGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = converted;
        }

        Mat mat = new Mat(bufferedImage.getHeight(),
                            bufferedImage.getWidth(),
                            CvType.CV_8UC3);

        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);

        return mat;
    }

    default Image matToImage(Mat image) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".bmp", image, buffer);

        byte[] data = buffer.toArray();
        buffer.release();

        return new Image(new ByteArrayInputStream(data));
    }

    default Rect customRectToRect(CustomRectangle rectangle) {
        return new Rect(rectangle.getX1(), rectangle.getY1(), rectangle.getWidth(), rectangle.getHeight());
    }
}
