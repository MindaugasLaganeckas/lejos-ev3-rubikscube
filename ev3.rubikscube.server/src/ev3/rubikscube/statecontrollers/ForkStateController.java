package ev3.rubikscube.statecontrollers;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class ForkStateController {
	
	private ForkPosition position;
	private ForkState state;
	private final RegulatedMotor backMotor;
	private final RegulatedMotor forkMotor;
	
	public ForkStateController(final ForkState initState, final ForkPosition initForkPosition, final RegulatedMotor backMotor) {
		this.state = initState;
		this.position = initForkPosition;
		this.backMotor = backMotor;
		this.forkMotor = Motor.B;
	}

	public void setNextPosition() {
		if (position == ForkPosition.HORIZONTAL) {
			position = ForkPosition.VERTICAL;
		} else if (position == ForkPosition.VERTICAL) {
			position = ForkPosition.HORIZONTAL;
		} 
	}
	
	public void setVerticalPosition() {
		if (position != ForkPosition.VERTICAL) {
			setStateToOff();
			forkMotor.rotate(-90);
			setStateToOn();
			position = ForkPosition.VERTICAL;
		}
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
