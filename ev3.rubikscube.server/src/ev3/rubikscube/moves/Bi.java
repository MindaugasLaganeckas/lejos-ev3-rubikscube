package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.supportingmoves.OppositeForkTurn;

public class Bi implements Move {

	private final OppositeForkTurn opositeForkTurn;
	private final CubeSideController controller;
	
	public Bi(final CubeSideController controller, final OppositeForkTurn opositeForkTurn) {
		this.opositeForkTurn = opositeForkTurn;
		this.controller = controller;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.B);
		opositeForkTurn.action();
	}
}
