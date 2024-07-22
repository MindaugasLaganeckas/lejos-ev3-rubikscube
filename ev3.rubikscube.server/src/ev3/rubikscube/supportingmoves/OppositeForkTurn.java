package ev3.rubikscube.supportingmoves;

import ev3.rubikscube.statecontrollers.ForkStateController;

public class OppositeForkTurn extends AbstractForkTurn {

	public OppositeForkTurn(final ForkStateController forkStateController) {
		super(forkStateController, -1);
	}
}
