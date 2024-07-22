package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.supportingmoves.ForkTurn;

public class B implements Move {

	private final CubeSideController controller;
	private final ForkTurn forkTurn;
	
	public B(final CubeSideController controller, final ForkTurn forkTurn) {
		this.controller = controller;
		this.forkTurn = forkTurn;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.B);
		forkTurn.action();
	}
}
