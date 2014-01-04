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

	public int area() {
		return this.dx * this.dy;
	}

	public Dimension2d dimension() {
		return Dimension2d.create(this.dx, this.dy);
	}

	public boolean contains(Box2d vertical) {
		return this.dx >= vertical.dx & this.dy >= vertical.dy;
	}

}
