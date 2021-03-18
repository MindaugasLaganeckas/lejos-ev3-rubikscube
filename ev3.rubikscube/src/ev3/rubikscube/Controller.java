package ev3.rubikscube;

import java.util.HashMap;
import java.util.Map;

import ev3.rubikscube.moves.*;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Controller {

	private static Map<String, Move> moveMap = new HashMap<>();
	
	public static void main(String[] args) throws InterruptedException {
		final RegulatedMotor leftSide = Motor.A;
		final RegulatedMotor rightSide = Motor.D;
		final RegulatedMotor forkMotor = Motor.B;
		final RegulatedMotor backMotor = Motor.C;

		initBigMotor(leftSide);
		initBigMotor(rightSide);
		initBigMotor(forkMotor);
		initBigMotor(backMotor);

		initMap();
		
		LCD.drawString("Press a key", 0, 1);
		Button.LEDPattern(6);
		Button.waitForAnyPress();
		
		final String solution = "U2 R2 B2 R  B2 R  F2 U  L2 D2 L' F2 L2 F2 U2";
		//final String solution = "F'";
		for (final String s: solution.split("\s+")) {
			moveMap.get(s).action();
		}
	}

	private static void initBigMotor(final RegulatedMotor motor) {
		motor.resetTachoCount();
		motor.rotateTo(0);
		motor.setSpeed(400);
		motor.setAcceleration(800);
	}
	
	private static void initMap() {
		final Up up = new Up();
		final Up2 up2 = new Up2();
		final Down down = new Down();
		final F f = new F();
		final Fi fi = new Fi();
		final F2 f2 = new F2(f);
		
		moveMap.put("B", new B(up2, f));
		moveMap.put("B2", new B2(up2, f2));
		moveMap.put("B'", new Bi(up2, fi));
		
		moveMap.put("D", new D(up, down, f));
		moveMap.put("D2", new D2(up, down, f2));
		moveMap.put("D'", new Di(up, down, fi));
		
		moveMap.put("F", f);
		moveMap.put("F2", f2);
		moveMap.put("F'", fi);
		
		moveMap.put("L", new L());
		moveMap.put("L2", new L2());
		moveMap.put("L'", new Li());
		
		moveMap.put("R", new R());
		moveMap.put("R2", new R2());
		moveMap.put("R'", new Ri());
		
		moveMap.put("U", new U(up, down, f));
		moveMap.put("U2", new U2(up, down, f2));
		moveMap.put("U'", new Ui(up, down, fi));
	}

}




