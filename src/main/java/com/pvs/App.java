package com.pvs;

import javafx.util.Pair;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author v.peschaniy
 *      Date: 15.10.2019
 */
public class App {

    public static void main(String[] args) throws IOException {
//        compare("D:/_ph/white.jpg", "D:/_ph/black.jpg");
//        System.out.println();
//        compare("D:/_ph/white.jpg", "D:/_ph/gray.jpg");
//        System.out.println();
//        compare("D:/_ph/black.jpg", "D:/_ph/gray.jpg");
//        compare("D:/_ph/same_1.jpg", "D:/_ph/same_2.jpg");
//        compare("D:/_ph/same_1.jpg", "D:/_ph/other_1.jpg");
//        compare("D:/_ph/img1.jpg", "D:/_ph/img2.jpg");
//        compare("D:/_ph/img2.jpg", "D:/_ph/img3.jpg");
//        compare("D:/_ph/img1.jpg", "D:/_ph/img3.jpg");

//        createImageStatistic("D:/_ph/img6.jpg").print();
//
//        fastDetector("D:/_ph/img6", PREFIX);
    }

    private static void compare(String path1, String path2) throws IOException {
        BufferedImage img1 = ImageIO.read(Paths.get(path1).toUri().toURL());
        BufferedImage img2 = ImageIO.read(Paths.get(path2).toUri().toURL());

        Accumulator colorAccum = compareImages(img1, img2);
        Accumulator grayscaleAccum = compareImagesGrayscale(img1, img2);

        System.out.println("diff between: " + path1 + " AND " + path2);

        System.out.println("diff[color]    : " + calculatePercents(img1, colorAccum.getTotal()) + "%");
        System.out.println("diff[grayscale]: " + calculatePercents(img1, grayscaleAccum.getTotal()) + "%");
    }

    private static long calculatePercents(BufferedImage img, long diff) {
        long maxDiff = img.getWidth() * img.getHeight() * 255L * 3L;
        return Math.round(100.0 * diff / maxDiff);
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

                int any = (r + g + b) / 3;
                converted.setRGB(i, j, new Color(any, any, any).getRGB());
            }
        }

        try (OutputStream out = new FileOutputStream(originPath + prefix + ".jpg")) {
            ImageIO.write(converted, "jpg", out);
        }
    }

    private static final String PREFIX = "_fast_step_16";

    private static void fastDetector(String originPath, String prefix) throws IOException {
        BufferedImage original = ImageIO.read(Paths.get(originPath + ".jpg").toUri().toURL());
        int height = original.getHeight();
        int width = original.getWidth();

        List<Pair<Integer, Integer>> redPixels = new ArrayList<>();
        int offset = 16;
        int pointRadius = 0;
        int step = 1;
//        int maxDiff = 100;

        BufferedImage converted = new BufferedImage(width, height, original.getType());
        converted.setData(original.getData());

        for (int y = offset; y < height - offset; y += step) {
            for (int x = offset; x < width - offset; x += step) {
                Color origin = new Color(original.getRGB(x, y));

                int counter = 0;

                for (int i = -offset; i <= offset; i += offset) {
                    int j = -i;
                    Color other = new Color(original.getRGB(x + i, y + j));
                    if (avgDiff(origin, other) > 100) {
                        counter++;
                    }
                }
                for (int i = -offset; i <= offset; i++) {

//                    for (int j = -offset; j <= offset; j++) {
//                        if (Math.abs(i) + Math.abs(j) == offset) {
//                            Color other = new Color(original.getRGB(x + i, y + j));
//                            if (avgDiff(origin, other) > 100) {
//                                counter++;
//                            }
//                        }
//                    }
                }

                if (counter >= 9) {
                    for (int i = -pointRadius; i <= pointRadius; i++) {
                        for (int j = -pointRadius; j <= pointRadius; j++) {
                            redPixels.add(new Pair<>(x + i, y + j));
                        }
                    }
                }
            }
        }

        System.out.println("redPixels = " + redPixels.size());

        for (Pair<Integer, Integer> xy : redPixels) {
            converted.setRGB(xy.getKey(), xy.getValue(), new Color(255, 0, 0).getRGB());
        }

        try (OutputStream out = new FileOutputStream(originPath + prefix + ".jpg")) {
            ImageIO.write(converted, "jpg", out);
        }
    }

    private static void convert(String originPath, String prefix) throws IOException {

        BufferedImage original = ImageIO.read(Paths.get(originPath + ".jpg").toUri().toURL());
        int height = original.getHeight();
        int width = original.getWidth();
        int progress = 0;

        BufferedImage converted = new BufferedImage(width, height, original.getType());

        for (int y = 1; y < height - 1; y++) {
            int newProgress = (int) (100.0 * y / height);
            if (newProgress > progress) {
                progress = newProgress;
                System.out.println("progress = " + progress);
            }
//            System.out.println("y = " + y);

            for (int x = 1; x < width - 1; x++) {

                Color tl = new Color(original.getRGB(x - 1, y - 1));
                Color tc = new Color(original.getRGB(x, y - 1));
                Color tr = new Color(original.getRGB(x + 1, y - 1));

                Color cl = new Color(original.getRGB(x - 1, y));
                Color cc = new Color(original.getRGB(x, y));        //current
                Color cr = new Color(original.getRGB(x + 1, y));

                Color bl = new Color(original.getRGB(x - 1, y - 1));
                Color bc = new Color(original.getRGB(x, y + 1));
                Color br = new Color(original.getRGB(x + 1, y + 1));

                int totalAvg = (avgDiff(cc, tl)
                        + avgDiff(cc, tc)
                        + avgDiff(cc, tr)
                        + avgDiff(cc, cl)
                        + avgDiff(cc, cr)
                        + avgDiff(cc, bl)
                        + avgDiff(cc, bc)
                        + avgDiff(cc, br)) / 8;

                if (totalAvg > 30) {
                    converted.setRGB(x, y, new Color(255, 0, 0).getRGB());
                }
                else {
                    converted.setRGB(x, y, cc.getRGB());
                }
            }
        }

        try (OutputStream out = new FileOutputStream(originPath + prefix + ".jpg")) {
            ImageIO.write(converted, "jpg", out);
        }
    }

    private static int avg(Color c) {
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    }

    private static int avgDiff(Color c1, Color c2) {
        return Math.abs(avg(c1) - avg(c2));
    }

    private static Accumulator compareImages(BufferedImage img1, BufferedImage img2) {
        Accumulator accumulator = new Accumulator();


        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                Color c1 = new Color(img1.getRGB(x, y));
                Color c2 = new Color(img2.getRGB(x, y));

                accumulator.addR(Math.abs(c1.getRed() - c2.getRed()));
                accumulator.addG(Math.abs(c1.getGreen() - c2.getGreen()));
                accumulator.addB(Math.abs(c1.getBlue() - c2.getBlue()));
            }
        }
        return accumulator;
    }

    private static Accumulator compareImagesGrayscale(BufferedImage img1, BufferedImage img2) {
        Accumulator accumulator = new Accumulator();

        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                Color c1 = new Color(img1.getRGB(x, y));
                Color c2 = new Color(img2.getRGB(x, y));

                int avg1 = (c1.getRed() + c1.getGreen() + c1.getBlue()) / 3;
                int avg2 = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 3;

                int diff = Math.abs(avg1 - avg2);

                accumulator.addR(diff);
                accumulator.addG(diff);
                accumulator.addB(diff);
            }
        }
        return accumulator;
    }


    private static Accumulator createImageStatistic(String path) throws IOException {
        BufferedImage img = ImageIO.read(Paths.get(path).toUri().toURL());
        System.out.println("statisticsFor: " + path);
        return createImageStatistic(img);
    }

    private static Accumulator createImageStatistic(BufferedImage img) {
        Accumulator accumulator = new Accumulator();

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = new Color(img.getRGB(x, y));

                accumulator.addR(c.getRed());
                accumulator.addG(c.getGreen());
                accumulator.addB(c.getBlue());
            }
        }
        return accumulator;
    }

    private static class Accumulator {
        long r;
        long g;
        long b;

        public void addRGB(Color color) {
            addR(color.getRed());
            addG(color.getGreen());
            addB(color.getBlue());
        }

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

        private BigDecimal getPercents(long part) {
            long total = getTotal();
            if (total == 0) {
                return BigDecimal.ZERO;
            }
            double percents = 100.0 * part / total;
            return BigDecimal.valueOf(percents)
                    .setScale(1, BigDecimal.ROUND_HALF_DOWN);
        }

        public void print() {
            System.out.println(String.format("R: %s\nG: %s\nB: %s", getPercents(r), getPercents(g), getPercents(b)));
        }
    }
}
