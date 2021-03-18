package ev3.rubikscube.moves;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class R2 implements Move {

	private final RegulatedMotor motor;
	
	public R2() {
		this.motor = Motor.D;
	}

	public void action() {
		motor.rotate(190);
		motor.rotate(-10);
	}
}
