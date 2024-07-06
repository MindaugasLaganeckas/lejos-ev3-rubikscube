package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;

public class Bi implements Move {

	private final Up2 up2;
	private final F f;
	
	public Bi(final Up2 up2, final F f) {
		this.up2 = up2;
		this.f = f;
	}

	@Override
	public void action() {
		up2.action();
		f.action();
		up2.action();
	}

}
