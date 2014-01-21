package pipi.gui;

import java.awt.Color;
import java.util.Collection;

import pipi.interval.Rectangle;

public class RectangleSet {

	private final Color color;
	private final Collection<Rectangle> rectangles;

	public RectangleSet(Color color, Collection<Rectangle> rectangles) {
		this.color = color;
		this.rectangles = rectangles;
	}

	public Color getColor() {
		return color;
	}

	public Collection<Rectangle> getRectangles() {
		return rectangles;
	}

}
