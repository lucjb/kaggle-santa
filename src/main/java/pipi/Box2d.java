package pipi;

public class Box2d {

	public final int dx;
	public final int dy;

	public Box2d(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public String toString() {
		return String.format("%dx%d", this.dx, this.dy);
	}

}
