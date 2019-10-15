import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.ImageWriter;
import ij.process.ImageConverter;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author v.peschaniy
 *      Date: 15.10.2019
 */
public class App {

    public static void main(String[] args) throws IOException {
        ImagePlus same1 = IJ.openImage("D:/_ph/same_1.jpg");
        ImagePlus same2 = IJ.openImage("D:/_ph/same_2.jpg");
        ImagePlus other1 = IJ.openImage("D:/_ph/other_1.jpg");
//
        Accumulator same1_vs_same2 = compareImages(same1, same2);
        Accumulator same1_vs_other1 = compareImages(same1, other1);

        int total = same1.getWidth() * same1.getHeight() * 3;
        int totalGrayscale = same1.getWidth() * same1.getHeight();

//
        System.out.println("color:");
        System.out.println(same1_vs_same2.getTotal() / total);
        System.out.println(same1_vs_other1.getTotal() / total);

        same1_vs_same2 = compareImagesGrayscale(same1, same2);
        same1_vs_other1 = compareImagesGrayscale(same1, other1);

        System.out.println("grayscale");
        System.out.println(same1_vs_same2.getTotal() / totalGrayscale);
        System.out.println(same1_vs_other1.getTotal() / totalGrayscale);
//

//
//        long same1_vs_same2_percentages = same1_vs_same2.getTotal() / total;
//        long same1_vs_other1_percentages = same1_vs_other1.getTotal() / total;
//
//        System.out.println("same1_vs_same2 = " + same1_vs_same2_percentages + "%");
//        System.out.println("same1_vs_other1 = " + same1_vs_other1_percentages + "%");

//        convertToGrayscaleAndSave("D:/_ph/same_1", "_white");
    }

    private static void convertToGrayscaleAndSave(String originPath, String prefix) throws IOException {
        BufferedImage original = ImageIO.read(Paths.get(originPath + ".jpg").toUri().toURL());
        BufferedImage converted = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {
                Color originPixel = new Color(original.getRGB(i, j));

                int r = originPixel.getRed();
                int g = originPixel.getGreen();
                int b = originPixel.getBlue();

//                int any = (r + g + b) / 3;
                int any = 128;
                converted.setRGB(i, j, new Color(any, any, any).getRGB());
            }
        }

        try (OutputStream out = new FileOutputStream(originPath + prefix + ".jpg")) {
            ImageIO.write(converted, "jpg", out);
        }
    }

    private static Accumulator compareImages(ImagePlus img1, ImagePlus img2) {
        Accumulator accumulator = new Accumulator();

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int[] p1 = img1.getPixel(x, y);
                int[] p2 = img2.getPixel(x, y);



                accumulator.addR(Math.abs(p1[0] - p2[0]));
                accumulator.addG(Math.abs(p1[1] - p2[1]));
                accumulator.addB(Math.abs(p1[2] - p2[2]));

//                int avg1 = (p1[0] + p1[1] + p1[2]) / 3;
//                int avg2 = (p2[0] + p2[1] + p2[2]) / 3;
//                accumulator.addR(Math.abs(avg1 - avg2));
            }
        }
        return accumulator;
    }
    private static Accumulator compareImagesGrayscale(ImagePlus img1, ImagePlus img2) {
        Accumulator accumulator = new Accumulator();

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                int[] p1 = img1.getPixel(x, y);
                int[] p2 = img2.getPixel(x, y);



//                accumulator.addR(Math.abs(p1[0] - p2[0]));
//                accumulator.addG(Math.abs(p1[1] - p2[1]));
//                accumulator.addB(Math.abs(p1[2] - p2[2]));

                int avg1 = (p1[0] + p1[1] + p1[2]) / 3;
                int avg2 = (p2[0] + p2[1] + p2[2]) / 3;
                accumulator.addR(Math.abs(avg1 - avg2));
            }
        }
        return accumulator;
    }


    private static Accumulator createImageStatistic(ImagePlus imp) {
        Accumulator accumulator = new Accumulator();

        for (int x = 0; x < imp.getWidth(); x++) {
            for (int y = 0; y < imp.getHeight(); y++) {
                int[] pixel = imp.getPixel(x, y);
                int r = pixel[0];
                int g = pixel[1];
                int b = pixel[2];

                accumulator.addR(r);
                accumulator.addG(g);
                accumulator.addB(b);

            }
        }
        return accumulator;
    }

    private static void convertToGrayscale(ImagePlus imp) {
        new ImageConverter(imp).convertToGray8();
    }

    private static class Accumulator {
        long r;
        long g;
        long b;

        public void addR(int r) {
            this.r += r;
        }

        public void addG(int g) {
            this.g += g;
        }

        public void addB(int b) {
            this.b += b;
        }

        @Override
        public String toString() {
            return r + "\t" + g + "\t" + b + "\t";
        }

        public long getTotal() {
            return r + g + b;
        }
    }
}
