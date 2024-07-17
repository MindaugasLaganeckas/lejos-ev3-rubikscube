package ev3.rubikscube.controller.frameprocessor.decorator;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ev3.rubikscube.controller.frameprocessor.CubeColors;
import ev3.rubikscube.controller.frameprocessor.FrameDecorator;
import ev3.rubikscube.controller.readcubecolors.CubeColorsReader;
import ev3.rubikscube.ui.IColorReadCompletedObserver;
import ev3.rubikscube.ui.IColorReadStartedObserver;
import ev3.rubikscube.ui.RubiksCubeColors;

public class ColorFrameDecorator implements IColorReadCompletedObserver, FrameDecorator, IColorReadStartedObserver {
	
	private RubiksCubeColors[] currentRead = null;
	
	@Override
	public synchronized void colorReadCompleted(final RubiksCubeColors[] colors) {
		this.currentRead = colors;
	}

	@Override
	public synchronized void newColorReadStarted() {
		this.currentRead = null;
	}
	
	@Override
	public synchronized Mat decorate(final Mat input) {
		final Mat output = input.clone();
		if (currentRead == null) return output;
		
		final List<Rect> calcPointsOfInterest = CubeColorsReader.calcAreasOfInterest(output.width(), output.height());
		
		for (int i = 0; i < calcPointsOfInterest.size(); i++) {
			final Rect rect = calcPointsOfInterest.get(i);
			final RubiksCubeColors color = currentRead[i];
			if (color.ordinal() < CubeColors.values().length) {
				Imgproc.rectangle(output, rect, CubeColors.values()[color.ordinal()].getColor(), -1);
			} else {
				Imgproc.rectangle(output, rect, new Scalar(255, 255, 255), -1);
			}
		}
		return output;
	}
}
