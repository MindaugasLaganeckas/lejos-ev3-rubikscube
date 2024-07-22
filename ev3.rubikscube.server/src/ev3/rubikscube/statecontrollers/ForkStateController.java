package ev3.rubikscube.statecontrollers;

import lejos.robotics.RegulatedMotor;

public class ForkStateController {
	
	private ForkState state;
	private final RegulatedMotor backMotor;
	
	public ForkStateController(final ForkState initState, final RegulatedMotor backMotor) {
		this.state = initState;
		this.backMotor = backMotor;
	}

	public void setStateToOn() {
		if (state != ForkState.ON) {
			backMotor.rotate(180);
			state = ForkState.ON;
		}
	}
	
	public void setStateToOff() {
		if (state != ForkState.OFF) {
			backMotor.rotate(180);
			state = ForkState.OFF;
		}
	}
}
