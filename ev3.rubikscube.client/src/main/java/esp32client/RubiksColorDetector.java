package esp32client;

import esp32client.enums.CubeColor;
import esp32client.util.GridCalculator;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class RubiksColorDetector {

    private final GridCalculator gridCalculator = new GridCalculator();

    public CubeColor[][] getDominantColorsInGrid(final Mat image, final int sideLength) {
        final CubeColor[][] colors = new CubeColor[3][3];

        this.gridCalculator.produceGrid(image, sideLength, (row, col, roi) -> {
            final Mat cell = image.submat(roi);
            colors[row + 1][col + 1] = getCellColor(cell);
            cell.release();
        });

        return colors;
    }

    private Scalar calculateDominantColor(final Mat slice) {
        // Force a copy to make the memory continuous
        final Mat continuousSlice = slice.clone();
        // Reshape image to a list of pixels (N rows, 3 columns for BGR)
        final Mat data = continuousSlice.reshape(1, (int) continuousSlice.total());
        data.convertTo(data, CvType.CV_32F);

        // K=1 for the single most dominant color
        // (Increase K if you want a palette of colors)
        final int k = 1;
        final Mat labels = new Mat();
        final TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 1.0);
        final Mat centers = new Mat();

        Core.kmeans(data, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);

        // Extract the BGR values from the center of the cluster
        final double b = centers.get(0, 0)[0];
        final double g = centers.get(0, 1)[0];
        final double r = centers.get(0, 2)[0];

        // Cleanup
        data.release();
        labels.release();
        centers.release();

        return new Scalar(b, g, r);
    }

    private CubeColor identifyColor(final Scalar bgrColor) {
        // 1. Convert BGR Scalar to HSV
        final Mat bgrMat = new Mat(1, 1, CvType.CV_8UC3, bgrColor);
        final Mat hsvMat = new Mat();
        Imgproc.cvtColor(bgrMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        final double[] hsv = hsvMat.get(0, 0);
        final double h = hsv[0];
        final double s = hsv[1];
        final double v = hsv[2];

        // 2. Check for White first (Low saturation + High value)
        if (s < 50 && v > 150) {
            return CubeColor.WHITE;
        }

        // 3. Logic for Hue-based colors
        if (h < 3 || h > 160) {
            return CubeColor.RED;
        }
        if (h < 25) {
            return CubeColor.ORANGE;
        }
        if (h < 40) {
            return CubeColor.YELLOW;
        }
        if (h < 85) {
            return CubeColor.GREEN;
        }
        if (h < 130) {
            return CubeColor.BLUE;
        }

        return CubeColor.WHITE; // Default fallback
    }

    /**
     * Integrates with the previous K-Means dominant color logic
     */
    private CubeColor getCellColor(final Mat cell) {
        // Use the K-Means method from the previous answer
        final Scalar dominantBgr = calculateDominantColor(cell);
        return identifyColor(dominantBgr);
    }
}
