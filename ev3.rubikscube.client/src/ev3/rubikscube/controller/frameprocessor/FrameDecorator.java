package ev3.rubikscube.controller.frameprocessor;

import org.opencv.core.Mat;

public interface FrameDecorator {
	Mat decorate(Mat input);
}
