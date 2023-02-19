package ev3.rubikscube.controller.readcubecolors;
import java.io.IOException;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import it.polito.elite.teaching.cv.RubiksCubeColors;
import it.polito.elite.teaching.cv.RubiksCubePlate;

public class ColorReadControllerForSides extends AbstractColorReadController {

	public ColorReadControllerForSides(MindstormRubiksCubeClient client) {
		super(client);
	}

	public void startReadSequence() {
		try {
			for (int i = 0; i < 4; i++) {
				mindstormRubiksCubeClient.sendCommand("F");
				mindstormRubiksCubeClient.sendCommand("UP");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finishReadSequence() {
		try {
			for (int i = 0; i < 4; i++) {
				mindstormRubiksCubeClient.sendCommand("DOWN");
				mindstormRubiksCubeClient.sendCommand("F'");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void colorReadCompleted(final Map<String, RubiksCubePlate> kubeColors, final ColorHitCounter colorHitCounter) {
		final RubiksCubeColors[] faceColors = new RubiksCubeColors[ColorHitCounter.NUMBER_OF_POINTS];
		for (int i = 0; i < ColorHitCounter.NUMBER_OF_POINTS; i++) {
			faceColors[i] = colorHitCounter.get(i);
		}
		final char sideCode = getSideName();
		switch (sideCode) {
		case 'B':
			kubeColors.get("L2").setColor(faceColors[1]);
			kubeColors.get("R8").setColor(faceColors[3]);
			kubeColors.get("R9").setColor(faceColors[6]);
			break;
		case 'U':
			kubeColors.get("L3").setColor(faceColors[2]);
			kubeColors.get("L6").setColor(faceColors[5]);
			kubeColors.get("L9").setColor(faceColors[8]);
			kubeColors.get("R6").setColor(faceColors[3]);
			kubeColors.get("R3").setColor(faceColors[6]);
			break;
		case 'F':
			kubeColors.get("L8").setColor(faceColors[1]);
			kubeColors.get("L7").setColor(faceColors[2]);
			kubeColors.get("R1").setColor(faceColors[6]);
			kubeColors.get("R2").setColor(faceColors[7]);
			break;
		case 'D':
			kubeColors.get("L4").setColor(faceColors[1]);
			kubeColors.get("L1").setColor(faceColors[2]);
			kubeColors.get("R4").setColor(faceColors[3]);
			kubeColors.get("R7").setColor(faceColors[6]);
			break;

		default:
			throw new RuntimeException("Unknown side " + sideCode);
		}
	}
}
