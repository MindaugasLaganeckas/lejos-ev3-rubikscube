package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.ForkStateController;
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
		forkStateController.setVerticalPosition();
		motor.rotate(-105);
		motor.rotate(15);
	}
}
