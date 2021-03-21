package ev3.rubikscube.moves;

import ev3.rubikscube.fork.ForkStateController;
import ev3.rubikscube.server.Move;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Up implements Move {

	private final RegulatedMotor leftMotor;
	private final RegulatedMotor rightMotor;
	private final ForkStateController forkStateController;
	
	public Up(final ForkStateController forkStateController) {
		this.leftMotor = Motor.A;
		this.rightMotor = Motor.D;
		this.forkStateController = forkStateController;
	}

	@Override
	public void action() {
		forkStateController.setStateToOff();
		
		leftMotor.synchronizeWith(new RegulatedMotor[] {rightMotor});
		leftMotor.startSynchronization();
		leftMotor.rotate(-90, true);
		rightMotor.rotate(-90, true);
		leftMotor.endSynchronization();
		leftMotor.waitComplete();
		rightMotor.waitComplete();
	}

}
