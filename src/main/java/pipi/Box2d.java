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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.dx;
		result = prime * result + this.dy;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Box2d other = (Box2d) obj;
		if (this.dx != other.dx)
			return false;
		if (this.dy != other.dy)
			return false;
		return true;
	}

	public int perimeter(){
		return this.dx + this.dx + this.dy + this.dy;
	}
	
}
