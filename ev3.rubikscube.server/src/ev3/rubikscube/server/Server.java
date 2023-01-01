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
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Server {

	private static final int FINISH_100 = 100;

	private static Map<Integer, String> communicationCodes = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
	{
		put(1, "B");
		put(2, "B2");
		put(3, "B'");

		put(4, "D");
		put(5, "D2");
		put(6, "D'");

		put(7, "F");
		put(8, "F2");
		put(9, "F'");

		put(10, "L");
		put(11, "L2");
		put(12, "L'");

		put(13, "R");
		put(14, "R2");
		put(15, "R'");

		put(16, "U");
		put(17, "U2");
		put(18, "U'");
	}};
	
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

        //System.out.println("Running...");
        final GraphicsLCD g = BrickFinder.getDefault().getGraphicsLCD();
        final int SW = g.getWidth();
        final int SH = g.getHeight();
        g.setFont(Font.getLargeFont());
        
		try (final ServerSocket ss = new ServerSocket(port)) {

	        while (true) {
	        	
	        	Button.LEDPattern(4);
		        g.clear();
		        g.refresh();
		        g.drawString("Waiting", SW/2, SH/2, GraphicsLCD.BASELINE|GraphicsLCD.HCENTER);
		        
				try(final Socket s = ss.accept();
						final DataInputStream din = new DataInputStream(s.getInputStream());
						final DataOutputStream dout = new DataOutputStream(s.getOutputStream());) {
					
			        g.clear();
			        g.refresh();
			        g.drawString("Connected", SW/2, SH/2, GraphicsLCD.BASELINE|GraphicsLCD.HCENTER);
			        Button.LEDPattern(0);
			        
					int code = 0;
					while (code != FINISH_100) {
						code = din.read();
						if (code != FINISH_100) {
							moveMap.get(communicationCodes.get(code)).action();							
						}
						dout.write(0);
						dout.flush();
					}
				}
			}
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
