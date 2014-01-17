package pipi.packer;

import java.util.Collection;
import java.util.List;

import pipi.Box2d;
import pipi.Dimension2d;
import pipi.OrientedDimension3d;
import pipi.OutputPresent;
import pipi.Point2d;
import pipi.interval.Positioning;
import pipi.interval.IntervalSlice;
import pipi.interval.MaximumRectangle;
import pipi.interval.PerimeterSlice;
import pipi.interval.PerimeterSnapshot;
import pipi.interval.PutRectangle;
import pipi.interval.Rectangle;
import pipi.main.BruteForce;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class IntervalPacker implements Packer {
	private static final class MaximumDimensionOrdering extends Ordering<Dimension2d> {
		@Override
		public int compare(Dimension2d left, Dimension2d right) {
			return Ints.compare(left.large, right.large);
		}
	}

	private static final class AreaOrdering extends Ordering<OrientedDimension3d> {
		@Override
		public int compare(OrientedDimension3d left, OrientedDimension3d right) {
			return Ints.compare(left.base.area(), right.base.area());
		}
	}

	public static final Ordering<OrientedDimension3d> AREA_ORDERING = new AreaOrdering();
	public static final Ordering<Dimension2d> MAXIMUM_DIMENSION_ORDERING = new MaximumDimensionOrdering();

	private IntervalSlice currentSlice = IntervalSlice.empty(1000, 1000);
	private PerimeterSlice perimeterSlice = new PerimeterSlice(1000, 1000);

	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalPacker() {
	}

	@Override
	public List<PutRectangle> packPesents(Collection<OrientedDimension3d> dimensions) {
		List<OrientedDimension3d> sortedDimensions = orderDimensions(dimensions);
		List<PutRectangle> result = Lists.newArrayList();

		for (OrientedDimension3d orientedDimension3d : sortedDimensions) {
			final Dimension2d base = orientedDimension3d.base;
			Collection<MaximumRectangle> maximumRectangles = this.currentSlice.getMaximumRectangles();
			Collection<MaximumRectangle> fittingRectangles = fittingRectangles(maximumRectangles, base);

			if (!fittingRectangles.isEmpty()) {
				Positioning bestInsertionPoint = bestPositioning(base, fittingRectangles);

				this.fillSlices(bestInsertionPoint.point2d, bestInsertionPoint.orientation);
				result.add(new PutRectangle(new Rectangle(bestInsertionPoint.point2d, bestInsertionPoint.orientation),
						orientedDimension3d.height));
			} else {
				return result;
			}
		}
		return result;
	}

	private Positioning bestPositioning(final Dimension2d base, Collection<MaximumRectangle> fittingRectangles) {
		// PerimeterSnapshot perimeterSnapshot =
		// this.perimeterSlice.perimeterSnapshot();
		PerimeterSnapshot perimeterSnapshot = null;
		List<Positioning> positionings = Lists.newArrayList();
		for (MaximumRectangle maximumRectangle : fittingRectangles) {
			Box2d vertical = base.vertical();
			Rectangle rectangle = maximumRectangle.rectangle;
			if (rectangle.getBox2d().contains(vertical)) {
				addOrientedInsertionPoints(positionings, rectangle, vertical, perimeterSnapshot);
			}
			Box2d horizontal = base.horizontal();
			if (maximumRectangle.rectangle.getBox2d().contains(horizontal) && !vertical.contains(horizontal)) {
				addOrientedInsertionPoints(positionings, rectangle, horizontal, perimeterSnapshot);

			}
		}
		Positioning bestInsertionPoint = new Ordering<Positioning>() {

			@Override
			public int compare(Positioning left, Positioning right) {
				return Ints.compare(left.perimeter, right.perimeter);
			}
		}.max(positionings);
		return bestInsertionPoint;
	}

	public List<OrientedDimension3d> orderDimensions(Collection<OrientedDimension3d> dimensions) {
		List<OrientedDimension3d> sortedDimensions = this.getDimensionsOrdering().reverse().sortedCopy(dimensions);
		return sortedDimensions;
	}

	public void fillSlices(Point2d point2d, Box2d orientation) {
		assert this.currentSlice.isFree(point2d.x, point2d.y, orientation.dx, orientation.dy);
		fillMainSlice(point2d, orientation);
		fillPerimeterSlice(point2d, orientation);
	}

	private void freeSlices(Point2d point2d, Box2d box2d) {
		freeMainSlice(point2d, box2d);
		freePerimeterSlice(point2d, box2d);
	}

	public void fillPerimeterSlice(Point2d point2d, Box2d orientation) {
		this.perimeterSlice.fill(point2d.x, point2d.y, orientation.dx, orientation.dy);
	}

	public void fillMainSlice(Point2d point2d, Box2d orientation) {
		this.currentSlice.fill(point2d.x, point2d.y, orientation.dx, orientation.dy);
	}

	public void freePerimeterSlice(Point2d point2d, Box2d orientation) {
		this.perimeterSlice.free(point2d.x, point2d.y, orientation.dx, orientation.dy);
	}

	public void freeMainSlice(Point2d point2d, Box2d orientation) {
		this.currentSlice.free(point2d.x, point2d.y, orientation.dx, orientation.dy);
	}

	private void addOrientedInsertionPoints(List<Positioning> positionings, Rectangle rectangle, Box2d vertical,
			PerimeterSnapshot perimeterSnapshot) {
		addInsertionPoint(positionings, rectangle.upperLeft(), vertical, perimeterSnapshot);
		if (rectangle.getBox2d().dx != vertical.dx) {
			addInsertionPoint(positionings, rectangle.upperRight(vertical), vertical, perimeterSnapshot);
		}
		if (rectangle.getBox2d().dy != vertical.dy) {
			addInsertionPoint(positionings, rectangle.bottomLeft(vertical), vertical, perimeterSnapshot);
			if (rectangle.getBox2d().dx != vertical.dx) {
				addInsertionPoint(positionings, rectangle.bottomRight(vertical), vertical, perimeterSnapshot);
			}
		}
	}

	private boolean addInsertionPoint(List<Positioning> positionings, Point2d point2d, Box2d vertical,
			PerimeterSnapshot perimeterSnapshot) {
		return positionings.add(new Positioning(point2d, vertical, getPerimeter(point2d, vertical)));
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

	protected Ordering<OrientedDimension3d> getDimensionsOrdering() {
		return new AreaOrdering();
		// return new MaximumDimensionOrdering();
	}

	public List<OutputPresent> getOutputPresents() {
		return this.outputPresents;
	}

	public int getPerimeter(Point2d point2d, Box2d box2d) {
		int perimeterInt = this.perimeterSlice.getPerimeterInt(point2d, box2d);
		// assert perimeterInt == BruteForce.
		return perimeterInt;
	}

	@Override
	public void freeAll(Collection<Rectangle> prefill) {
		for (Rectangle rectangle : prefill) {
			freeSlices(rectangle.point2d, rectangle.box2d);
		}
	}

	@Override
	public boolean isEmpty() {
		return this.currentSlice.isEmpty();
	}

}
