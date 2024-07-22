package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.supportingmoves.DoubleForkTurn;

public class U2 implements Move {

	private final DoubleForkTurn doubleForkTurn;
	private final CubeSideController controller;
	
	public U2(final CubeSideController controller, final DoubleForkTurn doubleForkTurn) {
		this.doubleForkTurn = doubleForkTurn;
		this.controller = controller;
	}

	@Override
	public void action() {
		controller.setDesiredState(CubeSideState.U);
		doubleForkTurn.action();
	}
}
