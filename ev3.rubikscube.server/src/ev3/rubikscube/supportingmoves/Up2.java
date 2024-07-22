package ev3.rubikscube.supportingmoves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.ForkStateController;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Up2 implements Move {

	private final RegulatedMotor leftMotor;
	private final RegulatedMotor rightMotor;
	private final ForkStateController forkStateController;
	
	public Up2(final ForkStateController forkStateController) {
		this.leftMotor = Motor.A;
		this.rightMotor = Motor.D;
		this.forkStateController = forkStateController;
	}

	@Override
	public void action() {
		forkStateController.setStateToOff();
		
		leftMotor.synchronizeWith(new RegulatedMotor[] {rightMotor});
		leftMotor.startSynchronization();
		leftMotor.rotate(-180, true);
		rightMotor.rotate(-180, true);
		leftMotor.endSynchronization();
		leftMotor.waitComplete();
		rightMotor.waitComplete();
	}
}
