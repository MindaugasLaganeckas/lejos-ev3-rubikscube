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
	public Mat decorate(Mat input) {
		final List<Point> pointsForColorTest = ColorHitCounter.calcPointsOfInterest(input.width(), input.height());

		final Mat dest = Mat.zeros(input.size(), CvType.CV_8UC3);
		if (!(lowerRanges.length() == upperRanges.length() && upperRanges.length() == CubeColors.values().length)) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < CubeColors.values().length; i++) {
			final int lowerRange = lowerRanges.get(i);
			final int upperRange = upperRanges.get(i);
			final CubeColors color = CubeColors.values()[i];
			final List<MatOfPoint> contoursOfColor = checkColor(input, pointsForColorTest, lowerRange, upperRange, color);
			Imgproc.drawContours(dest, contoursOfColor, -1, color.getColor(), -1);
		}
		// checking extra time for read
		{
			final int lowerRange = 140;
			final int upperRange = 240;
			final CubeColors color = CubeColors.RED;
			final List<MatOfPoint> contoursOfColor = checkColor(input, pointsForColorTest, lowerRange, upperRange, color);
			Imgproc.drawContours(dest, contoursOfColor, -1, color.getColor(), -1);
		}
		
		createFrameWithDots(dest, pointsForColorTest);
		return dest;
	}

	private List<MatOfPoint> checkColor(Mat input, final List<Point> pointsForColorTest, final int lowerRange,
			final int upperRange, final CubeColors color) {
		final List<MatOfPoint> contoursOfColor = findContoursWithinRange(input, lowerRange, upperRange);
		if (colorHitCounter != null) {
			for(final MatOfPoint contour : contoursOfColor) {
				final MatOfPoint2f dst = new MatOfPoint2f();
				contour.convertTo(dst, CvType.CV_32F);
				
				for (int j = 0; j < ColorHitCounter.NUMBER_OF_POINTS; j++) {
					final Point p = pointsForColorTest.get(j);
					if (Imgproc.pointPolygonTest(dst, p, false) >= 0) {
						colorHitCounter.inc(color, j);
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
		final Mat hsv = input.clone();
		
		Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);
		
		Core.inRange(hsv, new Scalar(lowerRange, 50, 20), new Scalar(upperRange, 255, 255), hsv);

		// Remove noise
		final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 50));
		Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_OPEN, kernel);
		
		// init
		final List<MatOfPoint> contours = new ArrayList<>();
		// Find contours
		Imgproc.findContours(hsv, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		return contours;
	}
}
