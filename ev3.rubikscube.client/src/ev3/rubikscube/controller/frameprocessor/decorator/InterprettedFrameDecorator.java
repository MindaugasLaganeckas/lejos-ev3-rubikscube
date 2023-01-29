package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;
import it.polito.elite.teaching.cv.RubiksCubeColors;

public class InterprettedFrameDecorator implements FrameDecorator {

	private final int[] lowerRanges;
	private final int[] upperRanges;
	
	private AtomicIntegerArray colors;
	
	public void resetCounters() {
		this.colors = new AtomicIntegerArray(CubeColors.values().length * Utils.NUMBER_OF_POINTS);
	}
	
	public InterprettedFrameDecorator(final int[] lowerRanges, final int[] upperRanges) {
		this.lowerRanges = lowerRanges;
		this.upperRanges = upperRanges;
		resetCounters();
	}
	
	@Override
	public Mat decorate(Mat input) {
		final List<Point> pointsForColorTest = Utils.calcPointsOfInterest(input.width(), input.height());

		final Mat dest = Mat.zeros(input.size(), CvType.CV_8UC3);
		if (!(lowerRanges.length == upperRanges.length && upperRanges.length == CubeColors.values().length)) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < CubeColors.values().length; i++) {
			final int lowerRange = lowerRanges[i];
			final int upperRange = upperRanges[i];
			final CubeColors color = CubeColors.values()[i];
			final List<MatOfPoint> contoursOfColor = findContoursWithinRange(input, lowerRange, upperRange);
			final List<Rect> rectsOfColor = new LinkedList<>();
			for (final MatOfPoint contour : contoursOfColor) {
				rectsOfColor.add(Imgproc.boundingRect(contour));
			}
			for(final Rect rect : rectsOfColor) {
				for (int j = 0; j < Utils.NUMBER_OF_POINTS; j++) {
					final Point p = pointsForColorTest.get(j);
					if (rect.contains(p)) {
						colors.getAndIncrement(j * CubeColors.values().length + i);
					}
				}
			}
			for (final Rect rect : rectsOfColor) {
				Imgproc.rectangle(dest, rect, color.getColor(), -1);
			}
		}
		createFrameWithDots(dest, pointsForColorTest);
		return dest;
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

		Core.inRange(hsv, new Scalar(lowerRange, 50, 50), new Scalar(upperRange, 255, 255), hsv); // hsv

		// Remove noise
		final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 50));
		Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_OPEN, kernel);
		
		// init
		final List<MatOfPoint> contours = new ArrayList<>();
		// Find contours
		Imgproc.findContours(hsv, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		return contours;
	}
	
	public RubiksCubeColors getColor(final int faceIndex) {
		int max = 0;
		int colorIndex = 0;
		final int length = CubeColors.values().length;
		for (int i = 0; i < length; i++) {
			final int value = colors.get(faceIndex * length + i);
			System.out.println("v " + value);
			if (max < value) {
				max = value;
				colorIndex = i;
				System.out.println("max " + value);
				System.out.println(i);
			}
		}
		System.out.println(colors);
		if (max < 10) {
			return RubiksCubeColors.WHITE;
		}
		return RubiksCubeColors.values()[colorIndex];
	}

	public void setLower(CubeColors color, int value) {
		lowerRanges[color.ordinal()] = value;
	}

	public void setUpper(CubeColors color, int value) {
		upperRanges[color.ordinal()] = value;
	}
}
