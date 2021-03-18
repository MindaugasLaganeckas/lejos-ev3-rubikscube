package ev3.rubikscube.moves;

public class Bi implements Move {

	private final Up2 up2;
	private final Fi fi;
	
	public Bi(final Up2 up2, final Fi fi) {
		this.up2 = up2;
		this.fi = fi;
	}

	@Override
	public void action() {
		up2.action();
		fi.action();
		up2.action();
	}

}
