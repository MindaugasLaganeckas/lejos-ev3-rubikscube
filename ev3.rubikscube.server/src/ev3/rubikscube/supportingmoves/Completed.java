package ev3.rubikscube.supportingmoves;

import ev3.rubikscube.server.Move;
import ev3.rubikscube.statecontrollers.CubeSideController;
import ev3.rubikscube.statecontrollers.CubeSideState;
import ev3.rubikscube.statecontrollers.ForkStateController;

public class Completed implements Move {

	private final ForkStateController forkStateController;
	private final CubeSideController cubeSideController;
	
	public Completed(final CubeSideController cubeSideController, final ForkStateController forkStateController) {
		this.forkStateController = forkStateController;
		this.cubeSideController = cubeSideController;
	}
	@Override
	public void action() {
		forkStateController.setStateToOff();
		cubeSideController.setState(CubeSideState.F);
	}
}
