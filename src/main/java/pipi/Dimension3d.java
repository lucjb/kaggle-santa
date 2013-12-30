package pipi;
import java.util.Arrays;

public class Dimension3d {

	public int small;
	public int medium;
	public int large;

	public Dimension3d(int small, int medium, int large) {
		assert small <= medium && medium <= large;
		this.small = small;
		this.medium = medium;
		this.large = large;
	}

	public static Dimension3d create(int i, int j, int k) {
		int[] array = new int[]{i, j, k};
		Arrays.sort(array);
		return new Dimension3d(array[0], array[1], array[2]);
	}
	
	@Override
	public String toString() {
		return String.format("%dx%dx%d", this.small, this.medium, this.large);
	}

}
