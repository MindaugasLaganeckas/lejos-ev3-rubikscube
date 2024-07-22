package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.ForkStateController;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class R implements Move {

	private final RegulatedMotor motor;
	private final ForkStateController forkStateController;
	
	public R(final ForkStateController forkStateController) {
		this.motor = Motor.D;
		this.forkStateController = forkStateController;
	}

	public void action() {
		forkStateController.setStateToOn();
		motor.rotate(-105);
		motor.rotate(15);
	}
}
