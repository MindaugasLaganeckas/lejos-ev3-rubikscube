package ev3.rubikscube.supportingmoves;

import ev3.rubikscube.statecontrollers.ForkStateController;

public class DoubleForkTurn extends AbstractForkTurn {

	public DoubleForkTurn(final ForkStateController forkStateController) {
		super(forkStateController, 1);
	}
	
	@Override
	public void action() {
		super.action();
		super.action();
	}
}
