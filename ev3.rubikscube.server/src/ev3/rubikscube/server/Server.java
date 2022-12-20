package ev3.rubikscube.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import ev3.rubikscube.fork.ForkState;
import ev3.rubikscube.fork.ForkStateController;
import ev3.rubikscube.moves.*;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Server {

	private static Map<String, Move> moveMap = new HashMap<>();

	private static final RegulatedMotor leftSide = Motor.A;
	private static final RegulatedMotor rightSide = Motor.D;
	private static final RegulatedMotor forkMotor = Motor.B;
	private static final RegulatedMotor backMotor = Motor.C;
	
	private static final int port = 3333; 
	
	public static void main(String[] args) throws Exception {
		final ForkStateController forkStateController = new ForkStateController(ForkState.OFF, backMotor);
		
		init(forkStateController);
		startServer(port);
	}

	private static void init(final ForkStateController forkStateController) {
		initBigMotor(leftSide);
		initBigMotor(rightSide);
		initBigMotor(forkMotor);
		initBigMotor(backMotor);
		
		initMap(forkStateController);
	}

	private static void startServer(final int port) throws Exception {

		try (final ServerSocket ss = new ServerSocket(port);
				final Socket s = ss.accept();
				final DataInputStream din = new DataInputStream(s.getInputStream());
				final DataOutputStream dout = new DataOutputStream(s.getOutputStream());) {
			int str = 0;
			while (str != 100) {
				str = din.read();
				dout.write(0);
				dout.flush();
				readCubeColors(din, dout);
			}
		}
	}

	private static void readCubeColors(final DataInputStream din, final DataOutputStream dout) throws Exception {
		
		final String turns = "DOWN DOWN DOWN DOWN";
		for (final String s : turns .split("\\s+")) {
			moveMap.get(s).action();
			// inform the client that the turn is complete
			dout.write(0);
			dout.flush();
			// wait for the client to read the colors
			final int responseCode = din.read();
			if (responseCode != 0) throw new RuntimeException(String.valueOf(responseCode));
		}
	}
	
	/**
	 * 
	 * @param solution E.g. "U2 R2 B2 R  B2 R  F2 U     L2 D2 L' F2 L2 F2 U2"
	 */
	private static void executeTurns(final String solution) {
		for (final String s : solution.split("\\s+")) {
			moveMap.get(s).action();
		}
	}

	private static void initBigMotor(final RegulatedMotor motor) {
		motor.resetTachoCount();
		motor.rotateTo(0);
		motor.setSpeed(400);
		motor.setAcceleration(800);
	}

	private static void initMap(final ForkStateController forkStateController) {
		
		
		
		final Up up = new Up(forkStateController);
		final Up2 up2 = new Up2(forkStateController);
		final Down down = new Down(forkStateController);
		final F f = new F(forkStateController);
		final Fi fi = new Fi(forkStateController);
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

		moveMap.put("L", new L(forkStateController));
		moveMap.put("L2", new L2(forkStateController));
		moveMap.put("L'", new Li(forkStateController));

		moveMap.put("R", new R(forkStateController));
		moveMap.put("R2", new R2(forkStateController));
		moveMap.put("R'", new Ri(forkStateController));

		moveMap.put("U", new U(up, down, f));
		moveMap.put("U2", new U2(up, down, f2));
		moveMap.put("U'", new Ui(up, down, fi));
		
		moveMap.put("DOWN", down);
	}

}
