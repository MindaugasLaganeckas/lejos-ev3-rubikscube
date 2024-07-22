package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;

public class Ui implements Move {

	private final CubeSideController controller;
	private final Fi fi;
	
	public Ui(final CubeSideController controller, final Fi fi) {
		this.controller = controller;
		this.fi = fi;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.U);
		fi.action();
	}
}
