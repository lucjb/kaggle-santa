package pipi.packer;

import java.util.Collection;
import java.util.List;

import pipi.Box2d;
import pipi.Dimension2d;
import pipi.OutputPresent;
import pipi.Point2d;
import pipi.interval.IntervalSlice;
import pipi.interval.MaximumRectangle;
import pipi.interval.PerimeterSlice;
import pipi.interval.PerimeterSnapshot;
import pipi.interval.Positioning;
import pipi.interval.Rectangle;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class BrunoPacker implements Packer {
	private static final class MaximumDimensionOrdering extends Ordering<Dimension2d> {
		@Override
		public int compare(Dimension2d left, Dimension2d right) {
			return Ints.compare(left.large, right.large);
		}
	}

	private static final class AreaOrdering extends Ordering<Dimension2d> {
		@Override
		public int compare(Dimension2d left, Dimension2d right) {
			return Ints.compare(left.area(), right.area());
		}
	}
	public static final Ordering<Dimension2d> AREA_ORDERING = new AreaOrdering();
	public static final Ordering<Dimension2d> MAXIMUM_DIMENSION_ORDERING = new MaximumDimensionOrdering();
	
	private IntervalSlice currentSlice = IntervalSlice.empty(1000, 1000);
	private PerimeterSlice perimeterSlice = new PerimeterSlice(1000, 1000);

	private int currentZ;
	private int lastZ = 0;
	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public BrunoPacker() {
		this.currentZ = 0;
	}

	/* (non-Javadoc)
	 * @see pipi.packer.Packer#packPesents(java.util.Collection)
	 */
	@Override
	public List<Rectangle> packPesents(Collection<Dimension2d> dimensions) {
		Multiset<Dimension2d> leftDimension2ds = HashMultiset.create(dimensions);
		List<Rectangle> result = Lists.newArrayList();
		
		while(!leftDimension2ds.isEmpty()){
			Collection<MaximumRectangle> maximumRectangles = this.currentSlice.getMaximumRectangles(this.perimeterSlice);
			double bestRatio = 0.0;
			Positioning bestInsertionPoint = null;
			Dimension2d bestDimension2d = null;
			for (Dimension2d dimension2d : leftDimension2ds) {
				Collection<MaximumRectangle> fittingRectangles = fittingRectangles(maximumRectangles, dimension2d);
				if (!fittingRectangles.isEmpty()) {
					Positioning partialBestInsertionPoint = bestPositioning(dimension2d, fittingRectangles);
					double thisRatio = (double)partialBestInsertionPoint.perimeter / partialBestInsertionPoint.orientation.perimeter();
					if(thisRatio > bestRatio){
						bestInsertionPoint = partialBestInsertionPoint;
						bestDimension2d = dimension2d;
						bestRatio = thisRatio;
					}else if(thisRatio == bestRatio && dimension2d.area() > bestDimension2d.area()){
						bestInsertionPoint = partialBestInsertionPoint;
						bestDimension2d = dimension2d;
						bestRatio = thisRatio;
					}
				} else {
					return result;
				}
			}
			Box2d orientation = bestInsertionPoint.orientation;
			this.fillSlices(bestInsertionPoint, orientation);
			result.add(new Rectangle(bestInsertionPoint.point2d, orientation));
			leftDimension2ds.remove(bestDimension2d);
		}
		return result;
	}

	private Positioning bestPositioning(final Dimension2d base, Collection<MaximumRectangle> fittingRectangles) {
//		PerimeterSnapshot perimeterSnapshot = this.perimeterSlice.perimeterSnapshot();
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

	public List<Dimension2d> orderDimensions(Collection<Dimension2d> dimensions) {
		List<Dimension2d> sortedDimensions = this.getDimensionsOrdering().reverse().sortedCopy(dimensions);
		return sortedDimensions;
	}

	public void fillSlices(Positioning bestInsertionPoint, Box2d orientation) {
		assert this.currentSlice.isFree(bestInsertionPoint.point2d.x, bestInsertionPoint.point2d.y, orientation.dx,
				orientation.dy);
		fillMainSlice(bestInsertionPoint, orientation);
		fillPerimeterSlice(bestInsertionPoint, orientation);
	}

	public void fillPerimeterSlice(Positioning bestInsertionPoint, Box2d orientation) {
		this.perimeterSlice.fill(bestInsertionPoint.point2d.x, bestInsertionPoint.point2d.y, orientation.dx, orientation.dy);
	}

	public void fillMainSlice(Positioning bestInsertionPoint, Box2d orientation) {
		this.currentSlice.fill(bestInsertionPoint.point2d.x, bestInsertionPoint.point2d.y, orientation.dx, orientation.dy);
	}

	private void addOrientedInsertionPoints(List<Positioning> positionings, Rectangle rectangle, Box2d vertical, PerimeterSnapshot perimeterSnapshot) {
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

	private boolean addInsertionPoint(List<Positioning> positionings, Point2d point2d, Box2d vertical, PerimeterSnapshot perimeterSnapshot) {
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

	protected Ordering<Dimension2d> getDimensionsOrdering() {
		return new AreaOrdering();
//		return new MaximumDimensionOrdering();
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
