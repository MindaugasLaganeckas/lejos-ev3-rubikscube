package ev3.rubikscube.controller.readcubecolors;

import java.io.IOException;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.ui.IColorReadCompletedObserver;
import ev3.rubikscube.ui.RubiksCubePlate;

public abstract class AbstractColorReadController implements IColorReadController, IColorReadCompletedObserver {

	protected int currentFaceIndex = 0;
	protected boolean startSequenceInitialized = false;
	protected final Map<String, RubiksCubePlate> kubeColors;
	
	/**
	 * Camera is facing first back 'B' side and then rotating U --> B etc
	 */
	protected char[] orderOfSidesToRead = {'B', 'U', 'F', 'D'};
	
	protected final MindstormRubiksCubeClient mindstormRubiksCubeClient;
	
	public AbstractColorReadController(final MindstormRubiksCubeClient client, final Map<String, RubiksCubePlate> kubeColors) {
		this.mindstormRubiksCubeClient = client;
		this.kubeColors = kubeColors;
	}
	
	protected char getSideName() {
		return orderOfSidesToRead[currentFaceIndex];
	}
	
	@Override
	public void turnToNextFace() {
		try {
			mindstormRubiksCubeClient.sendCommand("DOWN");
			currentFaceIndex++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected boolean startSequenceInitialized() {
		return startSequenceInitialized;
	}
	protected void initializedStartSequence() {
		initializedStartSequenceInternal();
		startSequenceInitialized = true;
	}
	
	protected boolean hasNextFaceToRead() {
		return currentFaceIndex < orderOfSidesToRead.length;
	}
	
	protected abstract void finishReadSequence();
	
	protected abstract void initializedStartSequenceInternal();
}
