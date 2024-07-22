package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.statecontrollers.ForkStateController;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class F implements Move {

	private final int direction = 1;
	private final RegulatedMotor forkMotor;
	private final ForkStateController forkStateController;
	private final CubeSideController controller;
	
	public F(final CubeSideController controller, final ForkStateController forkStateController) {
		this.forkMotor = Motor.B;
		this.forkStateController = forkStateController;
		this.controller = controller;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.F);
		rotate(direction);
	}

	public void rotate(final int direction) {
		forkStateController.setStateToOn();
		forkMotor.rotate(direction * -45);
		forkStateController.setStateToOff();
		forkMotor.rotate(direction * 95);
		forkStateController.setStateToOn();
		forkMotor.rotate(direction * -60);
		forkMotor.rotate(direction * 10);
	}
}
