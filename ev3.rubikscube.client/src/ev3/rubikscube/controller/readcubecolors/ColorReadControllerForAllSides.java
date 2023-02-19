package ev3.rubikscube.controller.readcubecolors;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import ev3.rubikscube.ui.RubiksCubePlate;

public class ColorReadControllerForAllSides implements IColorReadController {

	private final MindstormRubiksCubeClient client;
	private ColorReadControllerForMainSides colorReadControllerForMainSides;
	private ColorReadControllerForSides colorReadControllerForSides;
	private boolean sideReadStarted = false;
	
	public ColorReadControllerForAllSides(final MindstormRubiksCubeClient client) {
		this.client = client;
		startReadSequence();
	}
	
	public void startReadSequence() {
		this.colorReadControllerForMainSides = new ColorReadControllerForMainSides(client);
		this.colorReadControllerForSides = new ColorReadControllerForSides(client);
		sideReadStarted = false;
	}
	
	@Override
	public boolean isReadSequenceCompleted() {
		return colorReadControllerForMainSides.isReadSequenceCompleted() && colorReadControllerForSides.isReadSequenceCompleted();
	}
	
	@Override
	public void setNextFaceToRead() {
		if (!colorReadControllerForMainSides.isReadSequenceCompleted()) {
			colorReadControllerForMainSides.setNextFaceToRead();
		} else {
			colorReadControllerForSides.setNextFaceToRead();
		}
	}

	@Override	
	public void colorReadCompleted(final Map<String, RubiksCubePlate> kubeColors, final ColorHitCounter colorHitCounter) {
		if (!colorReadControllerForMainSides.isReadSequenceCompleted()) {
			colorReadControllerForMainSides.colorReadCompleted(kubeColors, colorHitCounter);
		} else {
			if (colorReadControllerForSides.isReadSequenceCompleted()) {
				colorReadControllerForSides.finishReadSequence();
			} else {
				if (!sideReadStarted) {
					colorReadControllerForSides.startReadSequence();
				}
				colorReadControllerForSides.colorReadCompleted(kubeColors, colorHitCounter);
			}
		}
	}
}
