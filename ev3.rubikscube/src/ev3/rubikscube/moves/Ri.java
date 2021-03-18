package ev3.rubikscube.moves;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Ri implements Move {

	private final RegulatedMotor motor;
	
	public Ri() {
		this.motor = Motor.D;
	}

	public void action() {
		motor.rotate(-100);
		motor.rotate(10);
	}
}
