package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.supportingmoves.ForkTurn;

public class F implements Move {

	private final ForkTurn forkTurn;
	private final CubeSideController controller;
	
	public F(final CubeSideController controller, final ForkTurn forkTurn) {
		this.forkTurn = forkTurn;
		this.controller = controller;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.F);
		forkTurn.action();
	}
}
