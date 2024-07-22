package ev3.rubikscube.statecontrollers;

import java.util.HashMap;
import java.util.Map;

import ev3.rubikscube.server.Move;

public class CubeSideController {
	
	private CubeSideState state;
	
	private final Map<String, Integer> transitions = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = 1L;
	{
		put("F->F", 0);
		put("F->U", 3);
		put("F->B", 2);
		put("F->D", 1);
		
		put("U->F", 1);
		put("U->U", 0);
		put("U->B", 3);
		put("U->D", 2);
		
		put("B->F", 2);
		put("B->U", 1);
		put("B->B", 0);
		put("B->D", 3);
		
		put("D->F", 3);
		put("D->U", 2);
		put("D->B", 1);
		put("D->D", 0);
	}};
	
	private final Move[] moveMap;
	private static final Move NO_MOVE = new Move() {
		@Override
		public void action() {}
	};
	
	public CubeSideController(final CubeSideState initState, final Move up, final Move up2, final Move down) {
		this.state = initState;
		this.moveMap = new Move[] {NO_MOVE, up, up2, down};
	}

	public void setDesiredState(final CubeSideState desiredSide) {
		final String transitionNeeded = state + "->" + desiredSide;
		if (transitions.containsKey(transitionNeeded)) {
			final int moveIndex = transitions.get(transitionNeeded);
			moveMap[moveIndex].action();
			this.state = desiredSide;
		} else {
			// e.g. F->R transition not needed and not possible
		}
	}
}
