package com.pvs.opencv; /**
 * @author v.peschaniy
 *      Date: 17.10.2019
 */

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CMP_EQ;
import static org.bytedeco.opencv.global.opencv_core.CV_8U;
import static org.bytedeco.opencv.global.opencv_cudaarithm.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/** Uses Harris Corner strength image to detect well localized corners,
 * replacing several closely located detections (blurred) by a single one.
 *
 * Based on C++ class from chapter 8. Used by `Ex2HarrisCornerDetector`.
 */
public class HarrisDetector {

    /** Neighborhood size for Harris edge detector. */
    int neighborhood = 3;
    /** Aperture size for Harris edge detector. */
    int aperture = 3;
    /** Harris parameter. */
    double k = 0.01;

    /** Maximum strength for threshold computations. */
    double maxStrength = 0.0;
    /** Size of kernel for non-max suppression. */
    int nonMaxSize = 3;

    /** Image of corner strength, computed by Harris edge detector. It is created by method `detect()`. */
    private Mat cornerStrength;
    /** Image of local corner maxima. It is created by method `detect()`. */
    private Mat localMax;


    /** Compute Harris corners.
     *
     * Results of computation can be retrieved using `getCornerMap` and `getCorners`.
     */

    public void detect(Mat image) {
        // Harris computations
        cornerStrength = new Mat();
        cornerHarris(image, cornerStrength, neighborhood, aperture, k);

        // Internal threshold computation.
        //
        // We will scale corner threshold based on the maximum value in the cornerStrength image.
        // Call to cvMinMaxLoc finds min and max values in the image and assigns them to output parameters.
        // Passing back values through function parameter pointers works in C bout not on JVM.
        // We need to pass them as 1 element array, as a work around for pointers in C API.
        DoublePointer maxStrengthA = new DoublePointer(maxStrength);
        minMaxLoc(
                cornerStrength,
                new DoublePointer(0.0) /* not used here, but required by API */,
                maxStrengthA, null, null, new Mat());

        // Read back the computed maxStrength
        maxStrength = maxStrengthA.get(0);

        // Local maxima detection.
        //
        // Dilation will replace values in the image by its largest neighbour value.
        // This process will modify all the pixels but the local maxima (and plateaus)
        Mat dilated = new Mat();
        dilate(cornerStrength, dilated, new Mat());
        localMax = new Mat();
        // Find maxima by detecting which pixels were not modified by dilation
        org.bytedeco.opencv.global.opencv_cudaarithm.compare(cornerStrength, dilated, localMax, CMP_EQ);
    }


    /** Get the corner map from the computed Harris values. Require call to `detect`.
     * @throws IllegalStateException if `cornerStrength` and `localMax` are not yet computed.
     */
    private Mat getCornerMap(double qualityLevel) {
        if (cornerStrength.empty() || localMax.empty()) {
            throw new IllegalStateException("Need to call `detect()` before it is possible to compute corner map.");
        }

        // Threshold the corner strength
        double t = qualityLevel * maxStrength;
        Mat cornerTh = new Mat();
        threshold(cornerStrength, cornerTh, t, 255, THRESH_BINARY);

        Mat cornerMap = new Mat();
        cornerTh.convertTo(cornerMap, CV_8U);

        // non-maxima suppression
        org.bytedeco.opencv.global.opencv_cudaarithm.bitwise_and(cornerMap, localMax, cornerMap);

        return cornerMap;
    }


    /** Get the feature points from the computed Harris values. Require call to `detect`. */
    public List<Point> getCorners(double qualityLevel) {
        // Get the corner map
        Mat cornerMap = getCornerMap(qualityLevel);
        // Get the corners
        return getCorners(cornerMap);
    }


    /** Get the feature points vector from the computed corner map.  */
    private List<Point> getCorners(Mat cornerMap) {

//        val i = cornerMap.createIndexer[UByteIndexer] ()

        UByteIndexer indexer = cornerMap.createIndexer(true);
        // Iterate over the pixels to obtain all feature points where matrix has non-zero values
        int width = cornerMap.cols();
        int height = cornerMap.rows();

        List<Point> result = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (indexer.get(x, y) != 0) {
                    result.add(new Point(x, y));
                }
            }
        }

        return result;
    }


    /**
     * Draw circles at feature point locations on an image
     */
    public void drawOnImage(Mat image, List<Point> points) {
        int radius = 4;
        int thickness = 1;
        Scalar color = new Scalar(255, 0, 0, 255);
        points.forEach(
                p -> circle(image, new Point(p.x(), p.y()), radius, color, thickness, 8, 0)
        );
    }
}