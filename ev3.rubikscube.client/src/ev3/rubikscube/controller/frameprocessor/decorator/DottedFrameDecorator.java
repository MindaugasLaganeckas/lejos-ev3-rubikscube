package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class DottedFrameDecorator implements FrameDecorator {

	protected int edgeLength = 210;

	@Override
	public Mat decorate(Mat input) {
		return createFrameWithDots(input, calcPointsOfInterest(input.width(), input.height()));
	}

	private Mat createFrameWithDots(Mat originalFrame, List<Point> pointsOfInterest) {
		final Mat clone = originalFrame.clone();
		final Scalar blue = new Scalar(255, 0, 0);
		pointsOfInterest.forEach(p -> {
			Imgproc.circle(clone, p, 5, blue, -1);
		});
		return clone;
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
