package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;

public class U implements Move {

	private final CubeSideController controller;
	private final F f;
	
	public U(final CubeSideController controller, final F f) {
		this.controller = controller;
		this.f = f;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.U);
		f.action();
	}
}
