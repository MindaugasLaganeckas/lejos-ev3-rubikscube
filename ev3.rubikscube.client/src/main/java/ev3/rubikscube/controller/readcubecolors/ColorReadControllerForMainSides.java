package ev3.rubikscube.controller.readcubecolors;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.ui.RubiksCubeColors;
import ev3.rubikscube.ui.RubiksCubePlate;

import static ev3.rubikscube.controller.readcubecolors.CubeColorsReader.NUMBER_OF_FACETS;

public class ColorReadControllerForMainSides extends AbstractColorReadController {
	
	public ColorReadControllerForMainSides(final MindstormRubiksCubeClient client, final Map<String, RubiksCubePlate> kubeColors) {
		super(client, kubeColors);
	}

	@Override	
	public void colorReadCompleted(final RubiksCubeColors[] faceColors) {
		if (!hasNextFaceToRead()) return;
		final String sideCode = String.valueOf(getSideName());
		for (int i = 0; i < NUMBER_OF_FACETS; i++) {
			try {
				kubeColors.get(sideCode + (i + 1)).setColor(getColor(faceColors, i, getSideName()));	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static RubiksCubeColors getColor(final RubiksCubeColors[] faceColors, final int index, final char sideCode) {
		// front face position when filmed with the camera
		if (sideCode == 'F' || sideCode == 'D' || sideCode == 'U') {
			return faceColors[NUMBER_OF_FACETS - 1 - index];
		}
		return faceColors[index];
	}
	
	@Override
	public void initializedStartSequenceInternal() {}
	
	@Override
	public void finishReadSequence() {}
}
