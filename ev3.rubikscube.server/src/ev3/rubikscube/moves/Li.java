package ev3.rubikscube.moves;

import ev3.rubikscube.fork.ForkStateController;
import ev3.rubikscube.server.Move;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Li implements Move {

	private final RegulatedMotor motor;
	private final ForkStateController forkStateController;
	
	public Li(final ForkStateController forkStateController) {
		this.motor = Motor.A;
		this.forkStateController = forkStateController;
	}

	public void action() {
		forkStateController.setStateToOn();
		motor.rotate(100);
		motor.rotate(-10);
	}
}
