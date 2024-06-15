package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class InterprettedFrameDecorator implements FrameDecorator {

	private final AtomicIntegerArray lowerRanges;
	private final AtomicIntegerArray upperRanges;
	
	private ColorHitCounter colorHitCounter = null;
	
	public synchronized void resetColorRead(final ColorHitCounter colorHitCounter) {
		this.colorHitCounter = colorHitCounter;
	}
	
	public InterprettedFrameDecorator(final AtomicIntegerArray lowerRanges, final AtomicIntegerArray upperRanges) {
		this.lowerRanges = lowerRanges;
		this.upperRanges = upperRanges;
	}
	
	@Override
	public Mat decorate(final Mat input) {
		final List<List<Point>> pointsForColorTest = ColorHitCounter.calcPointsOfInterest(input.width(), input.height());

		final Mat dest = Mat.zeros(input.size(), CvType.CV_8UC3);
		if (!(lowerRanges.length() == upperRanges.length() && upperRanges.length() == CubeColors.values().length)) {
			throw new IllegalArgumentException();
		}

		for (int facetIndex = 0; facetIndex < pointsForColorTest.size(); facetIndex++) {
			final List<Point> facetTestPoints = pointsForColorTest.get(facetIndex);
			
			for (int i = 0; i < CubeColors.values().length; i++) {
				final int lowerRange = lowerRanges.get(i);
				final int upperRange = upperRanges.get(i);
				final CubeColors color = CubeColors.values()[i];
				
				final List<MatOfPoint> contoursOfColor = checkColor(facetIndex, input, facetTestPoints, lowerRange, upperRange, color);
				Imgproc.drawContours(dest, contoursOfColor, -1, color.getColor(), -1);
			}
			// checking extra time for red
			{
				final int lowerRange = 120;
				final int upperRange = 240;
				final CubeColors color = CubeColors.RED;
				final List<MatOfPoint> contoursOfColor = checkColor(facetIndex, input, facetTestPoints, lowerRange, upperRange, color);
				Imgproc.drawContours(dest, contoursOfColor, -1, color.getColor(), -1);
			}
		}
				
		createFrameWithDots(dest, ColorHitCounter.calcPointsOfInterestFlat(input.width(), input.height()));
		return dest;
	}

	private List<MatOfPoint> checkColor(final int facetIndex, final Mat input, final List<Point> pointsForColorTest, final int lowerRange,
			final int upperRange, final CubeColors color) {
		final List<MatOfPoint> contoursOfColor = findContoursWithinRange(input, lowerRange, upperRange);
		if (colorHitCounter != null) {
			for(final MatOfPoint contour : contoursOfColor) {
				final MatOfPoint2f dst = new MatOfPoint2f();
				contour.convertTo(dst, CvType.CV_32F);
				
				for (int j = 0; j < pointsForColorTest.size(); j++) {
					final Point p = pointsForColorTest.get(j);
					if (Imgproc.pointPolygonTest(dst, p, false) >= 0) {
						colorHitCounter.inc(color, facetIndex);
					}
				}
			}	
		}
		return contoursOfColor;
	}
	
	private void createFrameWithDots(final Mat frame, final List<Point> pointsOfInterest) {
		final Scalar white = new Scalar(255, 255, 255);
		pointsOfInterest.forEach(p -> {
			Imgproc.circle(frame, p, 5, white, -1);
		});
	}	

	private List<MatOfPoint> findContoursWithinRange(final Mat input, final int lowerRange, final int upperRange) {
		final Mat illuminationCompensation = illuminationCompensation(input);
		
		final Mat hsv = histogramEqualization(illuminationCompensation);
		
		Core.inRange(hsv, new Scalar(lowerRange, 50, 50), new Scalar(upperRange, 255, 255), hsv);

		// Remove noise
		final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(30, 30));
		Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_OPEN, kernel);
		
		// init
		final List<MatOfPoint> contours = new ArrayList<>();
		// Find contours
		Imgproc.findContours(hsv, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		return contours;
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
}
