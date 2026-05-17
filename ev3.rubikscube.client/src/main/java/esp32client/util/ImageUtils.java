package esp32client.util;

import esp32client.enums.CubeColor;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static Mat inputStreamToMat(final InputStream in) throws IOException {
        // 1. Read all bytes from the InputStream
        final byte[] bytes = in.readAllBytes();

        // 2. Wrap bytes in an OpenCV MatOfByte
        final MatOfByte matOfByte = new MatOfByte(bytes);

        // 3. Decode the memory buffer into a standard Mat (Image)
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);
    }

    public static void flipImage(final Mat input, final Mat destination, final boolean horizontal) {
        final int flipCode = horizontal ? 1 : 0;
        Core.flip(input, destination, flipCode);
    }

    public static BufferedImage matToBufferedImage(final Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        final int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        final byte[] b = new byte[bufferSize];
        matrix.get(0, 0, b);
        final BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    private static Scalar getBgrFromEnum(final CubeColor color) {
        return switch (color) {
            case WHITE -> new Scalar(255, 255, 255);
            case YELLOW -> new Scalar(0, 255, 255);
            case RED -> new Scalar(0, 0, 255);
            case ORANGE -> new Scalar(0, 165, 255);
            case GREEN -> new Scalar(0, 255, 0);
            case BLUE -> new Scalar(255, 0, 0);
        };
    }

    public static void overlayDetectedColors(final Mat image, final CubeColor[][] grid, final int sideLength, final boolean showVerticalCenterOnly) {
        final GridCalculator gridCalculator = new GridCalculator();
        gridCalculator.produceGrid(image, sideLength, (row, col, roi) -> {
            if (showVerticalCenterOnly) {
                if (col == 0) {
                    overlaySingleRectangle(image, grid, row, col, roi);
                }
            } else {
                overlaySingleRectangle(image, grid, row, col, roi);
            }
        });
    }

    private static void overlaySingleRectangle(final Mat image, final CubeColor[][] grid, final int row, final int col, final Rect roi) {
        // Get the enum for this specific cell (adjusting indices for 0-2 range)
        // Assuming your grid is stored as [row+1][col+1] or similar
        final CubeColor colorEnum = grid[row + 1][col + 1];
        // Convert Enum to BGR Scalar
        final Scalar bgr = getBgrFromEnum(colorEnum);
        // Use -1 for thickness to fill the shape
        Imgproc.rectangle(image, roi, bgr, -1);
        // Optional: Draw a thin black border around the filled square for clarity
        Imgproc.rectangle(image, roi, new Scalar(0, 0, 0), 1);
    }
}
