public class Point {

	int x;
	int y;
	int z;

	public Point(int x, int y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return "(" + x + " " + y + " " + z + ")";
	}

}
