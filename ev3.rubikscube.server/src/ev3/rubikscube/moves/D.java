package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;

public class D implements Move {

	private final Up upToFront;
	private final Down bottomToFront;
	private final F f;
	
	public D(final Up upToFront, final Down bottomToFront, final F f) {
		this.upToFront = upToFront;
		this.bottomToFront = bottomToFront;
		this.f = f;
	}

	@Override
	public void action() {
		bottomToFront.action();
		f.action();
		upToFront.action();
	}

}
