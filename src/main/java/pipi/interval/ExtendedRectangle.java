package pipi.interval;

import com.google.common.primitives.Ints;

public class ExtendedRectangle implements Comparable<ExtendedRectangle>{

	public final Rectangle rectangle;
	public final int height;

	public ExtendedRectangle(Rectangle newRectangle, int height) {
		this.rectangle = newRectangle;
		this.height = height;
	}

	@Override
	public int compareTo(ExtendedRectangle o) {
		return Ints.compare(this.height, o.height);
	}

	@Override
	public String toString() {
		return "ExtendedRectangle [rectangle=" + this.rectangle + ", height=" + this.height + "]";
	}

	

}
