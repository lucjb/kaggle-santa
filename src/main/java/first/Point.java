package first;
import pipi.PresentDimension;


public class Point {

	public int x;
	public int y;
	public int z;

	public Point(int x, int y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return "(" + this.x + " " + this.y + " " + this.z + ")";
	}

	public PresentDimension dimensionDifference(Point min) {
		int i = this.x-min.x + 1;
		int j = this.y - min.y + 1;
		int k = this.z - min.z + 1;
		return PresentDimension.create(i, j, k);
	}

}
