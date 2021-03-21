package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;

public class D2 implements Move {

	private final Up upToFront;
	private final Down bottomToFront;
	private final F2 f2;
	
	public D2(final Up upToFront, final Down bottomToFront, final F2 f2) {
		this.upToFront = upToFront;
		this.bottomToFront = bottomToFront;
		this.f2 = f2;
	}

	@Override
	public void action() {
		bottomToFront.action();
		f2.action();
		upToFront.action();
	}

}
