package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ImageProcessingUtils {
	
	public static Mat histogramEqualization(final Mat input) {
		final Mat hsvImage = new Mat();
        Imgproc.cvtColor(input, hsvImage, Imgproc.COLOR_BGR2HSV);
        Core.normalize(hsvImage, hsvImage, 0, 255, Core.NORM_MINMAX);

        // Split the HSV image into its channels
        final List<Mat> hsvChannels = new ArrayList<>();
        Core.split(hsvImage, hsvChannels);

        // Equalize the histogram of the V channel
        Imgproc.equalizeHist(hsvChannels.get(2), hsvChannels.get(2));

        // Merge the channels back
        Core.merge(hsvChannels, hsvImage);
        return hsvImage;
	}
	
	public static Mat illuminationCompensation(final Mat image) {
        // Convert the image to LAB color space
		final Mat clone = image.clone();
        final Mat labImage = new Mat();
        Imgproc.cvtColor(clone, labImage, Imgproc.COLOR_BGR2Lab);

        // Split the LAB image into its channels
        final List<Mat> labChannels = new ArrayList<>();
        Core.split(labImage, labChannels);

        // Apply histogram equalization to the L channel
        Imgproc.equalizeHist(labChannels.get(0), labChannels.get(0));

        // Merge the channels back
        Core.merge(labChannels, labImage);

        // Convert back to BGR color space
        Imgproc.cvtColor(labImage, clone, Imgproc.COLOR_Lab2BGR);

        return clone;
	}
}
