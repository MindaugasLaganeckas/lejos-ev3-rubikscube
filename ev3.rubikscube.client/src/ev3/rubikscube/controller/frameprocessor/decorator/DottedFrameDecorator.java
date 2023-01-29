package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class DottedFrameDecorator implements FrameDecorator {

	protected int edgeLength = 100;

	@Override
	public Mat decorate(Mat input) {
		return createFrameWithDots(input, ColorHitCounter.calcPointsOfInterest(input.width(), input.height()));
	}

	private Mat createFrameWithDots(Mat originalFrame, List<Point> pointsOfInterest) {
		final Mat clone = originalFrame.clone();
		final Scalar blue = new Scalar(255, 0, 0);
		pointsOfInterest.forEach(p -> {
			Imgproc.circle(clone, p, 5, blue, -1);
		});
		return clone;
	}
		
}
