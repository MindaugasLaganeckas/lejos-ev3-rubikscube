package ev3.rubikscube.controller.frameprocessor;

import org.opencv.core.Scalar;

public enum CubeColors {

	RED      (new Scalar(0, 0, 255)),
	ORANGE   (new Scalar(0, 100, 255)),
	YELLOW   (new Scalar(0, 255, 255)),
	GREEN    (new Scalar(0, 255, 0)),
	BLUE     (new Scalar(255, 0, 0));
	
	private Scalar color;
	private CubeColors(Scalar color) {
		this.color = color;
	}
	
	public Scalar getColor() {
		return color;
	}
}
