package ev3.rubikscube.supportingmoves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.ForkStateController;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class AbstractForkTurn implements Move {

	private final int direction;
	private final RegulatedMotor forkMotor;
	private final ForkStateController forkStateController;
	
	public AbstractForkTurn(final ForkStateController forkStateController, final int direction) {
		this.forkMotor = Motor.B;
		this.forkStateController = forkStateController;
		this.direction = direction;
	}

	@Override
	public void action() {
		rotate(direction);
	}

	public void rotate(final int direction) {
		forkStateController.setStateToOn();
		forkMotor.rotate(direction * -45);
		forkStateController.setStateToOff();
		forkMotor.rotate(direction * 95);
		forkStateController.setStateToOn();
		forkMotor.rotate(direction * -60);
		forkMotor.rotate(direction * 10);
	}
}
