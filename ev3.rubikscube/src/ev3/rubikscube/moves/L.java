package ev3.rubikscube.moves;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class L implements Move {

	private final RegulatedMotor motor;
	
	public L() {
		this.motor = Motor.A;
	}

	public void action() {
		motor.rotate(-100);
		motor.rotate(10);
	}
}
