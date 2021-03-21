package ev3.rubikscube.moves;

import ev3.rubikscube.fork.ForkStateController;
import ev3.rubikscube.server.Move;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class R2 implements Move {

	private final RegulatedMotor motor;
	private final ForkStateController forkStateController;
	
	public R2(final ForkStateController forkStateController) {
		this.motor = Motor.D;
		this.forkStateController = forkStateController;
	}

	public void action() {
		forkStateController.setStateToOn();
		motor.rotate(190);
		motor.rotate(-10);
	}
}
