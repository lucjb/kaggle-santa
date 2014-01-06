package pipi;

public class Point2d {
	public final int x;
	public final int y;

	public Point2d(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public String toString() {
		return String.format("(%d,%d)", this.x, this.y);
	}
	
}
