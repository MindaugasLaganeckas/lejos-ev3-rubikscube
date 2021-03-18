package ev3.rubikscube.moves;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Fi implements Move {

	private final RegulatedMotor forkMotor;
	private final RegulatedMotor backMotor;
	private final int direction = 1;
	
	public Fi() {
		this.forkMotor = Motor.B;
		this.backMotor = Motor.C;
	}

	@Override
	public void action() {
		forkMotor.rotate(direction * -45);
		backMotor.rotate(180);
		forkMotor.rotate(direction * 95);
		backMotor.rotate(180);
		forkMotor.rotate(direction * -60);
		forkMotor.rotate(direction * 10);
	}
}
