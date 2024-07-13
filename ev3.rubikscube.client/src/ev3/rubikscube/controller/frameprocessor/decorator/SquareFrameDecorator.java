package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class SquareFrameDecorator implements FrameDecorator {

	@Override
	public Mat decorate(final Mat input) {
		return createFrameWithDots(input, ColorHitCounter.calcPointsOfInterest(input.width(), input.height()), new Scalar(255, 0, 0));
	}

	protected Mat createFrameWithDots(final Mat originalFrame, final List<List<Point>> pointsOfInterestHirarchy, final Scalar color) {
		final Mat clone = originalFrame.clone();
		pointsOfInterestHirarchy.forEach(pointsOfInterest -> {
			Imgproc.rectangle(clone, pointsOfInterest.get(0), pointsOfInterest.get(pointsOfInterest.size() - 1), color);
		});
		return clone;
	}
		
}
