package ev3.rubikscube.controller.readcubecolors;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import ev3.rubikscube.controller.frameprocessor.decorator.ProcessedFrameDecorator;
import ev3.rubikscube.ui.RubiksCubePlate;

public class ColorReadControllerForAllSides implements IColorReadController {

	private AbstractColorReadController[] colorReadControllers;
	private int currentControllerIndex = 0;
	private final PropertyChangeListener colorReadListener;
	private final ProcessedFrameDecorator decorator;
	
	public ColorReadControllerForAllSides(final MindstormRubiksCubeClient client, final PropertyChangeListener colorReadListener, final ProcessedFrameDecorator decorator) {
		this.colorReadListener = colorReadListener;
		this.decorator = decorator;
		this.colorReadControllers = new AbstractColorReadController[] {
				new ColorReadControllerForMainSides(client), new ColorReadControllerForSides(client)};
	}
	
	private void initReadOfCurrentFace() {
		final ColorHitCounter counter = new ColorHitCounter(colorReadListener);
		decorator.resetColorRead(counter);
	}
	
	public void startRead() {
		if (currentControllerIndex < colorReadControllers.length) {
			final AbstractColorReadController currentController = this.colorReadControllers[currentControllerIndex];
			if (!currentController.startSequenceInitialized()) {
				currentController.initializedStartSequence();
			}
			initReadOfCurrentFace();
		} else {
			colorReadListener.propertyChange(new PropertyChangeEvent(colorReadListener, "We are done", new Object(), new Object()));
		}
	}
	
	@Override
	public void turnToNextFace() {
		if (currentControllerIndex < colorReadControllers.length) {
			final AbstractColorReadController currentController = this.colorReadControllers[currentControllerIndex];
			if (currentController.hasNextFaceToRead()) {
				currentController.turnToNextFace();
				initReadOfCurrentFace();
			} else {
				currentController.finishReadSequence();
				currentControllerIndex++;
				startRead();
			}
		} else {
			throw new RuntimeException();
		}
	}
	
	@Override	
	public void colorReadCompleted(final Map<String, RubiksCubePlate> kubeColors, final ColorHitCounter colorHitCounter) {
		if (currentControllerIndex < colorReadControllers.length) {
			final AbstractColorReadController currentController = this.colorReadControllers[currentControllerIndex];
			currentController.colorReadCompleted(kubeColors, colorHitCounter);
		} else {
			throw new RuntimeException();
		}
	}
}
