package ev3.rubikscube.moves;

public class F2 implements Move {

	private final F f;

	public F2(final F f) {
		this.f = f;
	}

	@Override
	public void action() {
		for (int i = 0; i < 2; i++) {
			f.action();	
		}
	}
}
