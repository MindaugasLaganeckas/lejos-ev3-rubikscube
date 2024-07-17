package ev3.rubikscube.controller.readcubecolors;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ev3.rubikscube.controller.MindstormRubiksCubeClient;
import ev3.rubikscube.ui.IColorReadCompletedObserver;
import ev3.rubikscube.ui.IColorReadStartedObserver;
import ev3.rubikscube.ui.RubiksCubeColors;
import ev3.rubikscube.ui.RubiksCubePlate;

public class ColorReadControllerForAllSides implements IColorReadController, IColorReadCompletedObserver {

	private final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
	private AbstractColorReadController[] colorReadControllers;
	private int currentControllerIndex = 0;
	private final PropertyChangeListener colorReadListener;
	private final IColorReadStartedObserver[] colorReadStartedObservers;
	private final CubeColorsReader reader;
	
	public ColorReadControllerForAllSides(final MindstormRubiksCubeClient client, 
			final PropertyChangeListener colorReadListener, 
			final CubeColorsReader reader, 
			final Map<String, RubiksCubePlate> kubeColors,
			final IColorReadStartedObserver[] colorReadStartedObservers) {
		this.colorReadListener = colorReadListener;
		this.reader = reader;
		this.colorReadControllers = new AbstractColorReadController[] {
				new ColorReadControllerForMainSides(client, kubeColors), new ColorReadControllerForSides(client, kubeColors)};
		this.colorReadStartedObservers = colorReadStartedObservers;
	}
	
	private void initReadOfCurrentFace() {
		reader.startColorRead();
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
			
			for (final IColorReadStartedObserver observer : colorReadStartedObservers) {
				observer.newColorReadStarted();
			}
			
			final AbstractColorReadController currentController = this.colorReadControllers[currentControllerIndex];
			if (currentController.hasNextFaceToRead()) {
				currentController.turnToNextFace();
				initReadOfCurrentFace();
			} else {
				currentController.finishReadSequence();
				currentControllerIndex++;
				startRead();
			}
		}
	}
	
	@Override	
	public void colorReadCompleted(final RubiksCubeColors[] faceColors) {
		if (currentControllerIndex < colorReadControllers.length) {
			final AbstractColorReadController currentController = colorReadControllers[currentControllerIndex];
			currentController.colorReadCompleted(faceColors);
			
			newSingleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					turnToNextFace();
				}
			});
		} else {
			throw new RuntimeException();
		}
	}
}
