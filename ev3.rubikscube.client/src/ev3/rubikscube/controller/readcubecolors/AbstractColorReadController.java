package ev3.rubikscube.controller.readcubecolors;

import java.io.IOException;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;

public abstract class AbstractColorReadController implements IColorReadController {

	protected int currentFaceIndex = 0;
	/**
	 * Camera is facing first back 'B' side and then rotating U --> B etc
	 */
	protected char[] orderOfSidesToRead = {'B', 'U', 'F', 'D'};
	
	protected final MindstormRubiksCubeClient mindstormRubiksCubeClient;
	
	protected boolean readStarted = false;
	
	public AbstractColorReadController(final MindstormRubiksCubeClient client) {
		this.mindstormRubiksCubeClient = client;
	}
	
	protected char getSideName() {
		return orderOfSidesToRead[currentFaceIndex];
	}
	
	@Override
	public boolean isReadSequenceCompleted() {
		return readStarted && currentFaceIndex == orderOfSidesToRead.length;
	}
	
	@Override
	public void setNextFaceToRead() {
		try {
			mindstormRubiksCubeClient.sendCommand("DOWN");
			currentFaceIndex++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
