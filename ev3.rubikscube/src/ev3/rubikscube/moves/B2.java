package ev3.rubikscube.moves;

public class B2 implements Move {

	private final Up2 up2;
	private final F2 f2;
	
	public B2(final Up2 up2, final F2 f2) {
		this.up2 = up2;
		this.f2 = f2;
	}

	@Override
	public void action() {
		up2.action();
		f2.action();
		up2.action();
	}

}
