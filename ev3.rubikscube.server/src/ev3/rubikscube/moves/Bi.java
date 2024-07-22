package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;

public class Bi implements Move {

	private final CubeSideController controller;
	private final Fi fi;
	
	public Bi(final CubeSideController controller, final Fi fi) {
		this.controller = controller;
		this.fi = fi;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.B);
		fi.action();
	}
}
