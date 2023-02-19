package ev3.rubikscube.controller.readcubecolors;

import java.util.Map;

import ev3.rubikscube.controller.frameprocessor.decorator.ColorHitCounter;
import it.polito.elite.teaching.cv.RubiksCubePlate;

public interface IColorReadController {

	boolean isReadSequenceCompleted();

	void setNextFaceToRead();

	void colorReadCompleted(Map<String, RubiksCubePlate> kubeColors, ColorHitCounter colorHitCounter);

}