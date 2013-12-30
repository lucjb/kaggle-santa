package pipi;

public class Box3d {

	public int dx;
	public int dy;
	public int dz;

	public Box3d(int dx, int dy, int dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	@Override
	public String toString() {
		return String.format("%dx%dx%d", this.dx, this.dy, this.dz);
	}

}
