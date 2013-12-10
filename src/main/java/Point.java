import java.util.Arrays;

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

	public Box boxDifference(Point min) {
		int[] array = new int[]{this.x-min.x + 1, this.y - min.y + 1, this.z - min.z + 1};
		Arrays.sort(array);
		return new Box(array[0], array[1], array[2]);
	}

}
