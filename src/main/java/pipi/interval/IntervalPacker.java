package pipi.interval;

import java.util.Collection;
import java.util.List;

import pipi.Box2d;
import pipi.Dimension2d;
import pipi.OutputPresent;
import pipi.Point2d;
import pipi.main.BruteForce;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class IntervalPacker {
	private IntervalSlice currentSlice = IntervalSlice.empty(1000, 1000);
	private PerimeterSlice perimeterSlice = new PerimeterSlice(1000, 1000);

	private int currentZ;
	private int lastZ = 0;
	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalPacker() {
		this.currentZ = 0;
	}

	public List<Rectangle> packPesents(Collection<Dimension2d> dimensions) {
		List<Dimension2d> sortedDimensions = orderDimensions(dimensions);
		List<Rectangle> result = Lists.newArrayList();

		for (Dimension2d dimension2d : sortedDimensions) {
			final Dimension2d base = dimension2d;
			Collection<MaximumRectangle> maximumRectangles = this.currentSlice.getMaximumRectangles(this.perimeterSlice);
			Collection<MaximumRectangle> fittingRectangles = fittingRectangles(maximumRectangles, base);

			if (!fittingRectangles.isEmpty()) {
				InsertionPoint bestInsertionPoint = bestInsertionPoint(base, fittingRectangles);

				Box2d orientation = bestInsertionPoint.orientation;

				this.fillSlices(bestInsertionPoint, orientation);
				result.add(new Rectangle(bestInsertionPoint.point2d, orientation));
			} else {
				return result;
			}
		}
		return result;
	}

	private InsertionPoint bestInsertionPoint(final Dimension2d base, Collection<MaximumRectangle> fittingRectangles) {
//		PerimeterSnapshot perimeterSnapshot = this.perimeterSlice.perimeterSnapshot();
		PerimeterSnapshot perimeterSnapshot = null;
		List<InsertionPoint> insertionPoints = Lists.newArrayList();
		for (MaximumRectangle maximumRectangle : fittingRectangles) {
			Box2d vertical = base.vertical();
			Rectangle rectangle = maximumRectangle.rectangle;
			if (rectangle.getBox2d().contains(vertical)) {
				addOrientedInsertionPoints(insertionPoints, rectangle, vertical, perimeterSnapshot);
			}
			Box2d horizontal = base.horizontal();
			if (maximumRectangle.rectangle.getBox2d().contains(horizontal) && !vertical.contains(horizontal)) {
				addOrientedInsertionPoints(insertionPoints, rectangle, horizontal, perimeterSnapshot);

			}
		}
		InsertionPoint bestInsertionPoint = new Ordering<InsertionPoint>() {

			@Override
			public int compare(InsertionPoint left, InsertionPoint right) {
				return Ints.compare(left.perimeter, right.perimeter);
			}
		}.max(insertionPoints);
		return bestInsertionPoint;
	}

	public List<Dimension2d> orderDimensions(Collection<Dimension2d> dimensions) {
		Ordering<Dimension2d> dimensionsOrdering = getDimensionsOrdering();
		List<Dimension2d> sortedDimensions = dimensionsOrdering.reverse().sortedCopy(dimensions);
		return sortedDimensions;
	}

	public void fillSlices(InsertionPoint bestInsertionPoint, Box2d orientation) {
		assert this.currentSlice.isFree(bestInsertionPoint.point2d.x, bestInsertionPoint.point2d.y, orientation.dx,
				orientation.dy);
		fillMainSlice(bestInsertionPoint, orientation);
		fillPerimeterSlice(bestInsertionPoint, orientation);
	}

	public void fillPerimeterSlice(InsertionPoint bestInsertionPoint, Box2d orientation) {
		this.perimeterSlice.fill(bestInsertionPoint.point2d.x, bestInsertionPoint.point2d.y, orientation.dx, orientation.dy);
	}

	public void fillMainSlice(InsertionPoint bestInsertionPoint, Box2d orientation) {
		this.currentSlice.fill(bestInsertionPoint.point2d.x, bestInsertionPoint.point2d.y, orientation.dx, orientation.dy);
	}

	private void addOrientedInsertionPoints(List<InsertionPoint> insertionPoints, Rectangle rectangle, Box2d vertical, PerimeterSnapshot perimeterSnapshot) {
		addInsertionPoint(insertionPoints, rectangle.upperLeft(), vertical, perimeterSnapshot);
		if (rectangle.getBox2d().dx != vertical.dx) {
			addInsertionPoint(insertionPoints, rectangle.upperRight(vertical), vertical, perimeterSnapshot);
		}
		if (rectangle.getBox2d().dy != vertical.dy) {
			addInsertionPoint(insertionPoints, rectangle.bottomLeft(vertical), vertical, perimeterSnapshot);
			if (rectangle.getBox2d().dx != vertical.dx) {
				addInsertionPoint(insertionPoints, rectangle.bottomRight(vertical), vertical, perimeterSnapshot);
			}
		}
	}

	private boolean addInsertionPoint(List<InsertionPoint> insertionPoints, Point2d point2d, Box2d vertical, PerimeterSnapshot perimeterSnapshot) {
		return insertionPoints.add(new InsertionPoint(point2d, vertical, getPerimeter(point2d, vertical)));
	}

	private Rectangle bestRectangle(Collection<MaximumRectangle> fittingRectangles) {
		return new Ordering<MaximumRectangle>() {

			@Override
			public int compare(MaximumRectangle left, MaximumRectangle right) {
				return Ints.compare(left.rectangle.getBox2d().area(), right.rectangle.getBox2d().area());
			}
		}.min(fittingRectangles).rectangle;
	}

	private Collection<MaximumRectangle> fittingRectangles(Collection<MaximumRectangle> maximumRectangles,
			final Dimension2d base) {
		return Collections2.filter(maximumRectangles, new Predicate<MaximumRectangle>() {
			@Override
			public boolean apply(MaximumRectangle rectangle) {
				return rectangle.rectangle.getBox2d().dimension().contains(base);
			}
		});
	}

	private Ordering<Dimension2d> getDimensionsOrdering() {
		return new Ordering<Dimension2d>() {

			@Override
			public int compare(Dimension2d left, Dimension2d right) {
				return Ints.compare(left.area(), right.area());
			}
		};
	}

	public int getLastZ() {
		return this.lastZ;
	}

	public int getCurrentZ() {
		return this.currentZ;
	}

	public List<OutputPresent> getOutputPresents() {
		return this.outputPresents;
	}

	public int getPerimeter(Point2d point2d, Box2d box2d) {
		int perimeterInt = this.perimeterSlice.getPerimeterInt(point2d, box2d);
		// assert perimeterInt == BruteForce.
		return perimeterInt;
	}
}
