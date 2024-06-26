package ev3.rubikscube.controller.readcubecolors;
import java.io.IOException;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import ev3.rubikscube.ui.RubiksCubeColors;
import ev3.rubikscube.ui.RubiksCubePlate;

public class ColorReadControllerForSides extends AbstractColorReadController {

	public ColorReadControllerForSides(MindstormRubiksCubeClient client) {
		super(client);
	}

	public void startReadSequence() {
		try {
			for (int i = 0; i < 4; i++) {
				mindstormRubiksCubeClient.sendCommand("F'");
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
				mindstormRubiksCubeClient.sendCommand("F");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void colorReadCompleted(final Map<String, RubiksCubePlate> kubeColors, final ColorHitCounter colorHitCounter) {
		readStarted = true;
		
		final RubiksCubeColors[] faceColors = new RubiksCubeColors[ColorHitCounter.NUMBER_OF_FACETS];
		for (int i = 0; i < ColorHitCounter.NUMBER_OF_FACETS; i++) {
			faceColors[i] = colorHitCounter.get(i);
		}
		final char sideCode = getSideName();
		System.out.println("Side " + sideCode);
		switch (sideCode) {
		case 'B':
			kubeColors.get("L2").setColor(faceColors[1]);
			kubeColors.get("R8").setColor(faceColors[5]);
			kubeColors.get("R7").setColor(faceColors[8]);
			break;
		case 'U':
			kubeColors.get("L1").setColor(faceColors[0]);
			kubeColors.get("L4").setColor(faceColors[3]);
			kubeColors.get("R4").setColor(faceColors[5]);
			kubeColors.get("L7").setColor(faceColors[6]);
			kubeColors.get("R1").setColor(faceColors[8]);
			break;
		case 'F':
			kubeColors.get("L9").setColor(faceColors[0]);
			kubeColors.get("L8").setColor(faceColors[1]);
			kubeColors.get("R2").setColor(faceColors[7]);
			kubeColors.get("R3").setColor(faceColors[8]);
			break;
		case 'D':
			kubeColors.get("L3").setColor(faceColors[0]);
			kubeColors.get("L6").setColor(faceColors[1]);
			kubeColors.get("R6").setColor(faceColors[5]);
			kubeColors.get("R9").setColor(faceColors[8]);
			break;

		default:
			throw new RuntimeException("Unknown side " + sideCode);
		}
	}
}
