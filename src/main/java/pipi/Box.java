package pipi;
import java.util.Arrays;

public class Box {

	public int dx;
	public int dy;
	public int dz;

	public Box(int small, int medium, int large) {
		this.dx = small;
		this.dy = medium;
		this.dz = large;
	}

	@Override
	public String toString() {
		return String.format("%dx%dx%d", this.dx, this.dy, this.dz);
	}

}
