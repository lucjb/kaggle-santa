package pipi.sleigh;

import java.util.Collection;

import pipi.interval.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class RectangleFloor implements Comparable<RectangleFloor> {
	private final int height;
	private final Collection<Rectangle> rectangles = Lists.newArrayList();

	public RectangleFloor(int height) {
		this.height = height;
	}

	@Override
	public int compareTo(RectangleFloor o) {
		return Ints.compare(this.height, o.height);
	}
	
	public Collection<Rectangle> getRectangles() {
		return this.rectangles;
	}

	public int getHeight() {
		return this.height;
	}
}
