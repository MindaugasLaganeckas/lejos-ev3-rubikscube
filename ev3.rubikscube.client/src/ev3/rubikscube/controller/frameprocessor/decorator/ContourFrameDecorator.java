package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;

public class ContourFrameDecorator implements FrameDecorator {

	private final int[] lowerRanges;
	private final int[] upperRanges;

	public ContourFrameDecorator(final int[] lowerRanges, final int[] upperRanges) {
		this.lowerRanges = lowerRanges;
		this.upperRanges = upperRanges;
	}
	
	@Override
	public Mat decorate(Mat input) {
		final Mat dest = Mat.zeros(input.size(), CvType.CV_8UC3);
		if (!(lowerRanges.length == upperRanges.length && upperRanges.length == CubeColors.values().length)) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < CubeColors.values().length; i++) {
			final int lowerRange = lowerRanges[i];
			final int upperRange = upperRanges[i];
			final CubeColors color = CubeColors.values()[i];
			final List<MatOfPoint> contoursOfColor = findContoursWithinRange(input, lowerRange, upperRange);
			Imgproc.drawContours(dest, contoursOfColor, -1, color.getColor());
		}
		return dest;
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

	public void setLower(CubeColors color, int value) {
		lowerRanges[color.ordinal()] = value;
	}

	public void setUpper(CubeColors color, int value) {
		upperRanges[color.ordinal()] = value;
	}
}
