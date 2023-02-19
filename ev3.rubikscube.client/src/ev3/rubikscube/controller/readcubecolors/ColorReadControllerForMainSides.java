package ev3.rubikscube.controller.readcubecolors;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import it.polito.elite.teaching.cv.RubiksCubeColors;
import it.polito.elite.teaching.cv.RubiksCubePlate;

public class ColorReadControllerForMainSides extends AbstractColorReadController {
	
	public ColorReadControllerForMainSides(MindstormRubiksCubeClient client) {
		super(client);
	}

	private RubiksCubeColors getColor(final RubiksCubeColors[] faceColors, final int index) {
		final char sideCode = getSideName();
		// front face position when filmed with the camera
		if (sideCode == 'F' || sideCode == 'D' || sideCode == 'U') {
			return faceColors[ColorHitCounter.NUMBER_OF_POINTS - 1 - index];
		} else if (sideCode == 'B') {
			return faceColors[index];
		}
		return faceColors[index];
	}

	@Override	
	public void colorReadCompleted(final Map<String, RubiksCubePlate> kubeColors, final ColorHitCounter colorHitCounter) {
		readStarted = true;
		
		final RubiksCubeColors[] faceColors = new RubiksCubeColors[ColorHitCounter.NUMBER_OF_POINTS];
		for (int i = 0; i < ColorHitCounter.NUMBER_OF_POINTS; i++) {
			faceColors[i] = colorHitCounter.get(i);
		}
		final String sideCode = String.valueOf(getSideName());
		for (int i = 0; i < ColorHitCounter.NUMBER_OF_POINTS; i++) {
			try {
				kubeColors.get(sideCode + (i + 1)).setColor(getColor(faceColors, i));	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
