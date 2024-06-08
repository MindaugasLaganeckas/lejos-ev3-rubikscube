package ev3.rubikscube.moves;

import ev3.rubikscube.server.Move;

public class Fi implements Move {

	private final int direction = -1;
	private final F f;
	
	public Fi(final F f) {
		this.f = f;
	}

	@Override
	public void action() {
		f.rotate(direction);
	}
}
