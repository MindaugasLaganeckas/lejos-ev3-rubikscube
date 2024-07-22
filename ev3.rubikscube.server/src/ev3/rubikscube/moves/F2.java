package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.supportingmoves.DoubleForkTurn;

public class F2 implements Move {

	private final DoubleForkTurn doubleForkTurn;
	private final CubeSideController controller;
	
	public F2(final CubeSideController controller, final DoubleForkTurn doubleForkTurn) {
		this.doubleForkTurn = doubleForkTurn;
		this.controller = controller;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.F);
		doubleForkTurn.action();
	}
}
