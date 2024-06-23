package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;
import ev3.rubikscube.ui.RubiksCubeColors;

public class ColorFrameDecorator implements FrameDecorator {
	
	private final ProcessedFrameDecorator processedFrameDecorator;
	public ColorFrameDecorator (final ProcessedFrameDecorator processedFrameDecorator) {
		this.processedFrameDecorator = processedFrameDecorator;
	}
	
	@Override
	public Mat decorate(final Mat input) {
		final List<List<Point>> calcPointsOfInterest = ColorHitCounter.calcPointsOfInterest(input.width(), input.height());
		final ColorHitCounter colorHitCounter = processedFrameDecorator.getColorHitCounter();
		final Mat clone = input.clone();
		
		for (int i = 0; i < calcPointsOfInterest.size(); i++) {
			final List<Point> points = calcPointsOfInterest.get(i);
			final RubiksCubeColors color = colorHitCounter.get(i);
			if (color.ordinal() < CubeColors.values().length) {
				Imgproc.circle(clone, points.get(4), 50, CubeColors.values()[color.ordinal()].getColor(), -1);	
			} else {
				Imgproc.circle(clone, points.get(4), 50, new Scalar(255, 255, 255), -1);
			}
		}
		return clone;
	}
	
	

}
