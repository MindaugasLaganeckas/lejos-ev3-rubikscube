package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;

public class U2 implements Move {

	private final CubeSideController controller;
	private final F2 f2;
	
	public U2(final CubeSideController controller, final F2 f2) {
		this.controller = controller;
		this.f2 = f2;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.U);
		f2.action();
	}
}
