package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;

public class U implements Move {

	private final Up upToFront;
	private final Down bottomToFront;
	private final F f;
	
	public U(final Up upToFront, final Down bottomToFront, final F f) {
		this.upToFront = upToFront;
		this.bottomToFront = bottomToFront;
		this.f = f;
	}

	@Override
	public void action() {
		upToFront.action();
		f.action();
		bottomToFront.action();
	}

}
