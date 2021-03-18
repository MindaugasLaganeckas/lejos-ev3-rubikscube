package ev3.rubikscube.moves;

public class B implements Move {

	private final Up2 up2;
	private final F f;
	
	public B(final Up2 up2, final F f) {
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
