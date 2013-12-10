package pipi;
import java.util.Arrays;

public class PresentDimension {

	public int small;
	public int medium;
	public int large;

	public PresentDimension(int small, int medium, int large) {
		assert small <= medium && medium <= large;
		this.small = small;
		this.medium = medium;
		this.large = large;
	}

	public static PresentDimension create(int i, int j, int k) {
		int[] array = new int[]{i, j, k};
		Arrays.sort(array);
		return new PresentDimension(array[0], array[1], array[2]);
	}
	
	@Override
	public String toString() {
		return String.format("%dx%dx%d", this.small, this.medium, this.large);
	}

}
