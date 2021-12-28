package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class InterprettedFrameDecorator implements FrameDecorator {

	private int[] lowerRanges = new int[] { 0, 10, 30, 70, 100, 250 };
	private int[] upperRanges = new int[] { 10, 30, 40, 80, 110, 255 };
	protected int edgeLength = 140;
	
	@Override
	public Mat decorate(Mat input) {
		final List<Point> pointsOfInterest = calcPointsOfInterest(input.width(), input.height());

		final Mat dest = Mat.zeros(input.size(), CvType.CV_8UC3);

		for (final Point p : pointsOfInterest) {
			boolean colorDetected = false;
			for (final CubeColors color : CubeColors.values()) {
				if (colorDetected) break;
				if (testPointColor(input, p, lowerRanges[color.ordinal()], upperRanges[color.ordinal()])) {
					drawRect(dest, color.getColor(), p);
					colorDetected = true;
				}
			}
			if (!colorDetected) {
				drawRect(dest, CubeColors.WHITE.getColor(), p);
			}
		}
		return dest;
	}

	private boolean testPointColor(final Mat input, final Point p, final int lowerRange, final int upperRange) {
		final Mat hsv = input.clone();
		Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);

		Core.inRange(hsv, new Scalar(lowerRange, 100, 20), new Scalar(upperRange, 255, 255), hsv); // hsv

		// init
		final List<MatOfPoint> contours = new ArrayList<>();

		// Find contours
		Imgproc.findContours(hsv, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		for (final MatOfPoint contour : contours) {
			if (Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), p, false) > 0) {
				return true;
			}
		}

		return false;
	}

	private void drawRect(Mat dest, Scalar color, final Point p) {
		final int delta = 60;
		Imgproc.rectangle(dest, new Point(p.x + delta, p.y + delta), new Point(p.x - delta, p.y - delta), color, -1);
	}

	public void setLower(CubeColors color, int value) {
		lowerRanges[color.ordinal()] = value;
	}

	public void setUpper(CubeColors color, int value) {
		upperRanges[color.ordinal()] = value;
	}
	
	protected List<Point> calcPointsOfInterest(final int frameWidth, final int frameHeight) {
		final List<Point> pointsOfInterest = new LinkedList<>();
		final Point center = new Point(frameWidth / 2, frameHeight / 2);
		pointsOfInterest.add(center);
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y - edgeLength));
		pointsOfInterest.add(new Point(center.x, center.y - edgeLength));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y - edgeLength));
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y));
		pointsOfInterest.add(new Point(center.x, center.y));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y));
		
		pointsOfInterest.add(new Point(center.x - edgeLength, center.y + edgeLength));
		pointsOfInterest.add(new Point(center.x, center.y + edgeLength));
		pointsOfInterest.add(new Point(center.x + edgeLength, center.y + edgeLength));
		
		return pointsOfInterest;
	}
}
