package com.pvs.opencv;

import javafx.util.Pair;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * @author v.peschaniy
 *      Date: 15.10.2019
 */
public class OpenCV {

    public static void main(String[] args) throws IOException {
        System.out.println("start");
        System.load("C:\\Windows\\System32\\opencv_java412.dll");
        Mat img = OpenCVUtilsJava.loadAndShowOrExit(new File("D:/_ph/img6.jpg"), IMREAD_GRAYSCALE);

        Mat dst = new Mat();

        imwrite("D:/_ph/img6_harris_____.jpg", dst);

        try {
            HarrisDetector harris = new HarrisDetector();
            // Compute Harris values
            harris.detect(img);
            // Detect Harris corners
            List<Point> corners = harris.getCorners(0.01);

            // Draw Harris corners
            harris.drawOnImage(img, corners);
            OpenCVUtilsJava.show(img, "Harris Corners");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
