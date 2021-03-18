package ev3.rubikscube.moves;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class L2 implements Move {

	private final RegulatedMotor motor;
	
	public L2() {
		this.motor = Motor.A;
	}

	public void action() {
		motor.rotate(-190);
		motor.rotate(10);
	}
}
