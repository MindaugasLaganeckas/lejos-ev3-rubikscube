package ev3.rubikscube.controller.frameprocessor.decorator;

import static ev3.rubikscube.controller.frameprocessor.decorator.ImageProcessingUtils.histogramEqualization;
import static ev3.rubikscube.controller.frameprocessor.decorator.ImageProcessingUtils.illuminationCompensation;
import static ev3.rubikscube.ui.RubiksCubeAppController.RED_COLLOR_LOWER_RANGE2;
import static ev3.rubikscube.ui.RubiksCubeAppController.RED_COLLOR_UPPER_RANGE2;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class ProcessedFrameDecorator implements FrameDecorator {

	private final AtomicIntegerArray lowerRanges;
	private final AtomicIntegerArray upperRanges;
	private final AtomicBoolean[] showFilters;
	private final AtomicInteger saturationValue;
	private final AtomicInteger valueValue;
	
	public ProcessedFrameDecorator(final AtomicIntegerArray lowerRanges, final AtomicIntegerArray upperRanges, 
			final AtomicBoolean[] showFilters, final AtomicInteger saturationValue, final AtomicInteger valueValue) {
		this.lowerRanges = lowerRanges;
		this.upperRanges = upperRanges;
		this.showFilters = showFilters;
		this.valueValue = valueValue;
		this.saturationValue = saturationValue;
	}
	
	@Override
	public Mat decorate(final Mat input) {
		if (!(lowerRanges.length() == upperRanges.length() && upperRanges.length() == CubeColors.values().length)) {
			throw new IllegalArgumentException();
		}

		final Mat illuminationCompensation = illuminationCompensation(input);
		final Mat hsvImage = histogramEqualization(illuminationCompensation);
		
		final Mat maskedImage = maskedImage(input, hsvImage);
		if (!maskedImage.empty()) {
			return maskedImage;	
		}
		Imgproc.cvtColor(hsvImage, hsvImage, Imgproc.COLOR_HSV2BGR);
		return hsvImage;
	}
	
	private Mat maskedImage(final Mat input, final Mat hsvImage) {
		
		final Mat result = Mat.zeros(input.rows(), input.cols(), input.type());
		final List<Mat> colorAreas = new LinkedList<Mat>();
		
		// RED color has two ranges to check for
		{
			final int redColorIndex = CubeColors.RED.ordinal();
	        // Define the range of red color in HSV
			final Scalar lowerRed1 = new Scalar(lowerRanges.get(redColorIndex), saturationValue.get(), valueValue.get());
			final Scalar upperRed1 = new Scalar(upperRanges.get(redColorIndex), 255, 255);
			final Scalar lowerRed2 = new Scalar(RED_COLLOR_LOWER_RANGE2, saturationValue.get(), valueValue.get());
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
			final Scalar lowerRange = new Scalar(lowerRanges.get(i), saturationValue.get(), valueValue.get());
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
		
		return result;
	}
}
