package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;

public class Di implements Move {

	private final Up upToFront;
	private final Down bottomToFront;
	private final Fi fi;
	
	public Di(final Up upToFront, final Down bottomToFront, final Fi fi) {
		this.upToFront = upToFront;
		this.bottomToFront = bottomToFront;
		this.fi = fi;
	}

	@Override
	public void action() {
		bottomToFront.action();
		fi.action();
		upToFront.action();
	}

}
