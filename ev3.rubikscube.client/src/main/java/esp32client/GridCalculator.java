package esp32client;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

public class GridCalculator {

    public void produceGrid(final Mat image, final int sideLength, final CellProcessor processor) {
        final int centerX = (image.cols() - sideLength) / 2;
        final int centerY = (image.rows() - sideLength) / 2;

        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                // Calculate coordinates (matching your detection logic)
                final int x = centerX + (col * sideLength);
                final int y = centerY + (row * sideLength);
                // Draw a filled rectangle
                final int padding = sideLength / 4;
                // Create a ROI (Region of Interest)
                final int width = sideLength - 2 * padding;
                final Rect roi = new Rect(x + padding, y + padding, width, width);
                processor.process(row, col, roi);
            }
        }
    }

    @FunctionalInterface
    public interface CellProcessor {
        void process(int row, int col, final Rect regionOfInterest);
    }
}
