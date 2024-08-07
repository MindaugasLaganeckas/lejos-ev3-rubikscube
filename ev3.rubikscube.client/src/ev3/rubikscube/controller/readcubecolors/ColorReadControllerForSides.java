package ev3.rubikscube.controller.readcubecolors;
import java.io.IOException;
import java.util.Map;

import static ev3.rubikscube.controller.readcubecolors.CubeColorsReader.NUMBER_OF_FACETS;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.ui.RubiksCubeColors;
import ev3.rubikscube.ui.RubiksCubePlate;

public class ColorReadControllerForSides extends AbstractColorReadController {

	public ColorReadControllerForSides(final MindstormRubiksCubeClient client, final Map<String, RubiksCubePlate> kubeColors) {
		super(client, kubeColors);
	}

	@Override
	public void initializedStartSequenceInternal() {
		try {
			for (int i = 0; i < 4; i++) {
				mindstormRubiksCubeClient.sendCommand("F'");
				mindstormRubiksCubeClient.sendCommand("UP");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
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
	public void colorReadCompleted(final RubiksCubeColors[] faceColors) {
		if (!hasNextFaceToRead()) return;
		for (int i = 0; i < NUMBER_OF_FACETS; i++) {
			System.out.print(faceColors[i].name() + " ");
		}
		System.out.println();
		final char sideCode = getSideName();
		System.out.println("Side " + sideCode);
		switch (sideCode) {
		case 'B':
			kubeColors.get("R3").setColor(faceColors[0]);
			kubeColors.get("R2").setColor(faceColors[3]);
			kubeColors.get("L8").setColor(faceColors[7]);
			break;
		case 'U':
			kubeColors.get("R1").setColor(faceColors[0]);
			kubeColors.get("R4").setColor(faceColors[3]);
			kubeColors.get("L4").setColor(faceColors[7]);
			kubeColors.get("L7").setColor(faceColors[8]);
			break;
		case 'F':
			kubeColors.get("R7").setColor(faceColors[0]);
			kubeColors.get("R8").setColor(faceColors[1]);
			kubeColors.get("L2").setColor(faceColors[7]);
			kubeColors.get("L1").setColor(faceColors[8]);
			break;
		case 'D':
			kubeColors.get("R9").setColor(faceColors[0]);
			kubeColors.get("L3").setColor(faceColors[2]);
			kubeColors.get("R6").setColor(faceColors[3]);
			kubeColors.get("L6").setColor(faceColors[5]);
			kubeColors.get("L9").setColor(faceColors[8]);
			break;

		default:
			throw new RuntimeException("Unknown side " + sideCode);
		}
	}
}
