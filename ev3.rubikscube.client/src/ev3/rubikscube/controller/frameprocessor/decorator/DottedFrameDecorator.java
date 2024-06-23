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
		return createFrameWithDots(input, ColorHitCounter.calcPointsOfInterestFlat(input.width(), input.height()), new Scalar(255, 0, 0));
	}

	protected Mat createFrameWithDots(Mat originalFrame, List<Point> pointsOfInterest, Scalar color) {
		final Mat clone = originalFrame.clone();
		pointsOfInterest.forEach(p -> {
			Imgproc.circle(clone, p, 5, color, -1);
		});
		return clone;
	}
		
}
