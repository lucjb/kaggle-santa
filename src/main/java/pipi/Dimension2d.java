package pipi;
import java.util.Arrays;

public class Dimension2d {

	public int small;
	public int large;

	public Dimension2d(int small, int large) {
		assert small <= large;
		this.small = small;
		this.large = large;
	}

	public static Dimension2d create(int i, int j) {
		int[] array = new int[]{i, j};
		Arrays.sort(array);
		return new Dimension2d(array[0], array[1]);
	}
	
	@Override
	public String toString() {
		return String.format("%dx%dx", this.small, this.large);
	}

	public boolean contains(Dimension2d base) {
		return this.small >= base.small && this.large >= base.large;
	}

	public Box2d horizontal(){
		return new Box2d(this.large, this.small);
	}

	public Box2d vertical(){
		return new Box2d(this.small, this.large);
	}
}
