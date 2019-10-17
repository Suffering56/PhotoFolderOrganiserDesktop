import com.sun.imageio.plugins.jpeg.JPEGImageReader;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * @author v.peschaniy
 *      Date: 17.10.2019
 */
public class AppV2 {

    private static final String INPUT_FILE_NAME = "D:/_ph/red.jpg";
    private static final String OUTPUT_FILE_NAME = "D:/_ph/red.jpg";

    public static void main(String[] args) throws IOException {
//        createImage(OUTPUT_FILE_NAME, 100, 100, getRGB(255, 0, 0));

        for (String readerFormatName : ImageIO.getReaderFormatNames()) {
            System.out.println("readerFormatName = " + readerFormatName);
        }

//        ImageIO.read()
        try (ImageInputStream in = ImageIO.createImageInputStream(new File(INPUT_FILE_NAME))) {
            int counter = 0;
            int read;
            while ((read = in.read()) != -1 && counter++ < 100) {
                System.out.println("read = " + read);
            }
        }
    }

    private static void createImage(String fileName, int width, int height, int rgb) throws IOException {
        BufferedImage converted = new BufferedImage(width, height, TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                converted.setRGB(x, y, rgb);
            }
        }

        ImageIO.write(converted, "jpg", new File(fileName));
    }

    private static int getRGB(int r, int g, int b) {
        return getA(255) | getR(r) | getG(g) | getB(b);
    }

    private static int getRGB(int r, int g, int b, int a) {
        return getA(a) | getR(r) | getG(g) | getB(b);
    }

    private static int getA(int a) {
        return getShiftVal(a, 24);
    }

    private static int getR(int r) {
        return getShiftVal(r, 16);
    }

    private static int getG(int g) {
        return getShiftVal(g, 8);
    }

    private static int getB(int b) {
        return getShiftVal(b, 0);
    }

    private static int getShiftVal(int val, int shift) {
        return (val & 0xFF) << shift;
    }
}
