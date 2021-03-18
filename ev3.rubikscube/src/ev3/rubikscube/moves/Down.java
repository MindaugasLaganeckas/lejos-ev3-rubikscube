package ev3.rubikscube.moves;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Down implements Move {

	private final RegulatedMotor leftMotor;
	private final RegulatedMotor rightMotor;
	private final RegulatedMotor backMotor;
	
	public Down() {
		this.leftMotor = Motor.A;
		this.rightMotor = Motor.D;
		this.backMotor = Motor.C;
	}

	@Override
	public void action() {
		backMotor.rotate(180);
		
		leftMotor.synchronizeWith(new RegulatedMotor[] {rightMotor});
		leftMotor.startSynchronization();
		leftMotor.rotate(90, true);
		rightMotor.rotate(90, true);
		leftMotor.endSynchronization();
		leftMotor.waitComplete();
		rightMotor.waitComplete();
		
		backMotor.rotate(180);
	}

}
