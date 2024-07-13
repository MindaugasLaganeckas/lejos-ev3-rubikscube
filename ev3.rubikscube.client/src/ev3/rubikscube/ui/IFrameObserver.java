package ev3.rubikscube.ui;

import org.opencv.core.Mat;

public interface IFrameObserver {
	void update(final Mat frame);
}