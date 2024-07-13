package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

import static ev3.rubikscube.ui.RubiksCubeAppController.RED_COLLOR_LOWER_RANGE2;
import static ev3.rubikscube.ui.RubiksCubeAppController.RED_COLLOR_UPPER_RANGE2;

public class ProcessedFrameDecorator implements FrameDecorator {

	private final AtomicIntegerArray lowerRanges;
	private final AtomicIntegerArray upperRanges;
	private final AtomicBoolean[] showFilters;
	
	private ColorHitCounter colorHitCounter = null;
	
	public synchronized void resetColorRead(final ColorHitCounter colorHitCounter) {
		this.colorHitCounter = colorHitCounter;
	}
	
	public ProcessedFrameDecorator(final AtomicIntegerArray lowerRanges, final AtomicIntegerArray upperRanges, 
			final AtomicBoolean[] showFilters) {
		this.lowerRanges = lowerRanges;
		this.upperRanges = upperRanges;
		this.showFilters = showFilters;
	}
	
	@Override
	public Mat decorate(final Mat input) {
		final List<List<Point>> pointsForColorTest = ColorHitCounter.calcPointsOfInterest(input.width(), input.height());

		if (!(lowerRanges.length() == upperRanges.length() && upperRanges.length() == CubeColors.values().length)) {
			throw new IllegalArgumentException();
		}

		final Mat illuminationCompensation = illuminationCompensation(input);
		final Mat hsvImage = histogramEqualization(illuminationCompensation);
		
		final Mat maskedImage = maskedImage(input, hsvImage, pointsForColorTest);
		if (!maskedImage.empty()) {
			return maskedImage;	
		}
		Imgproc.cvtColor(hsvImage, hsvImage, Imgproc.COLOR_HSV2BGR);
		return hsvImage;
	}
	
	private void checkColor(final int facetIndex, final Mat input, final List<Point> pointsForColorTest, final CubeColors color) {
		if (getColorHitCounter() != null) {
			for (final Point p : pointsForColorTest) {
				double[] hsv = input.get((int)p.y, (int)p.x);
				if (colorExists(hsv)) {
					getColorHitCounter().inc(color, facetIndex);
				}
			}	
		}
	}

	private boolean colorExists(double[] hsv) {
		return (int)hsv[1] != 0;
	}

	private Mat maskedImage(final Mat input, final Mat hsvImage, final List<List<Point>> pointsForColorTest) {
		
		final Mat result = Mat.zeros(input.rows(), input.cols(), input.type());
		final List<Mat> colorAreas = new LinkedList<Mat>();
		
		// RED color has two ranges to check for
		{
			final int redColorIndex = CubeColors.RED.ordinal();
	        // Define the range of red color in HSV
			final Scalar lowerRed1 = new Scalar(lowerRanges.get(redColorIndex), 50, 50);
			final Scalar upperRed1 = new Scalar(upperRanges.get(redColorIndex), 255, 255);
			final Scalar lowerRed2 = new Scalar(RED_COLLOR_LOWER_RANGE2, 50, 50);
			final Scalar upperRed2 = new Scalar(RED_COLLOR_UPPER_RANGE2, 255, 255);
			
	        // Threshold the HSV image to get only red colors
	        final Mat mask1 = new Mat();
	        Core.inRange(hsvImage, lowerRed1, upperRed1, mask1);
	        final Mat mask2 = new Mat();
	        Core.inRange(hsvImage, lowerRed2, upperRed2, mask2);
	        // Combine the masks
	        final Mat redMask = new Mat();
	        Core.add(mask1, mask2, redMask);
	        
	        // Bitwise-AND mask and original image
	        final Mat redAreas = new Mat();
	        Core.bitwise_and(input, input, redAreas, redMask);
	        
	        if (showFilters[redColorIndex].get()) {
	        	Core.bitwise_or(redAreas, result, result);
			}
	        colorAreas.add(redAreas);
		}
		// RED is the first color in color and we have already checked for it
		for (int i = 1; i < CubeColors.values().length; i++) {
			final Scalar lowerRange = new Scalar(lowerRanges.get(i), 50, 50);
			final Scalar upperRange = new Scalar(upperRanges.get(i), 255, 255);
			
			final Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerRange, upperRange, mask);
	        
	        final Mat areas = new Mat();
	        Core.bitwise_and(input, input, areas, mask);
	        if (showFilters[i].get()) {
	        	Core.bitwise_or(areas, result, result);
			}
	        
	        colorAreas.add(areas);
		}
		
		for (int facetIndex = 0; facetIndex < pointsForColorTest.size(); facetIndex++) {
			final List<Point> facetTestPoints = pointsForColorTest.get(facetIndex);
			for (int colorIndex = 0; colorIndex < CubeColors.values().length; colorIndex++) {
				final CubeColors color = CubeColors.values()[colorIndex];
				checkColor(facetIndex, colorAreas.get(colorIndex), facetTestPoints, color);
			}
		}

		return result;
	}
	
	private static Mat histogramEqualization(final Mat input) {
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
	
	private static Mat illuminationCompensation(final Mat image) {
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

	public ColorHitCounter getColorHitCounter() {
		return colorHitCounter;
	}
}
