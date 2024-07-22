package ev3.rubikscube.supportingmoves;

import ev3.rubikscube.statecontrollers.ForkStateController;

public class ForkTurn extends AbstractForkTurn {

	public ForkTurn(final ForkStateController forkStateController) {
		super(forkStateController, 1);
	}
}
