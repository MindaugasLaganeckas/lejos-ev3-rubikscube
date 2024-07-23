package ev3.rubikscube.controller.frameprocessor.decorator;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.FrameDecorator;
import ev3.rubikscube.controller.readcubecolors.CubeColorsReader;

public class SquareFrameDecorator implements FrameDecorator {

	private static final int THICKNESS = 10;
	
	@Override
	public Mat decorate(final Mat input) {
		final Mat clone = input.clone();
		CubeColorsReader.calcAreasOfInterest(input.width(), input.height()).forEach(rect -> {
			Imgproc.rectangle(clone, rect, new Scalar(255, 0, 0), THICKNESS);
		});
		return clone;
	}
}
