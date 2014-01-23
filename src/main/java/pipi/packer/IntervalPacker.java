package pipi.packer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import pipi.Box2d;
import pipi.Dimension2d;
import pipi.OrientedDimension3d;
import pipi.OutputPresent;
import pipi.Point2d;
import pipi.interval.Interval;
import pipi.interval.MaximumRectangle;
import pipi.interval.Positioning;
import pipi.interval.PutRectangle;
import pipi.interval.Rectangle;
import pipi.interval.slice.HeightMisfit;
import pipi.interval.slice.HeightSlice;
import pipi.interval.slice.IntervalSlice;
import pipi.interval.slice.PerimeterSlice;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class IntervalPacker implements Packer {
	private final class SameHeightPositioning extends Ordering<Positioning> {
		@Override
		public int compare(Positioning left, Positioning right) {
			return Ints.compare(left.heightMisfit.same, right.heightMisfit.same);
		}
	}

	private final class BestHeightPositioning extends Ordering<Positioning> {
		@Override
		public int compare(Positioning left, Positioning right) {
			return -Ints.compare(left.heightMisfit.none, right.heightMisfit.none);
		}
	}

	private static final class PerimeterPositioning extends Ordering<Positioning> {
		@Override
		public int compare(Positioning left, Positioning right) {
			return Ints.compare(left.perimeter, right.perimeter);
		}
	}

	private static final class BoundariesSidesPositioning extends Ordering<Positioning> {
		@Override
		public int compare(Positioning left, Positioning right) {
			return Ints.compare(left.boundaries.sides(), right.boundaries.sides());
		}
	}

	private static final class MixedPositioning extends Ordering<Positioning> {
		@Override
		public int compare(Positioning left, Positioning right) {
			return Ints
					.compare(left.perimeter - left.boundaries.perimeter(), right.perimeter - right.boundaries.perimeter());
		}
	}

	private final class HeightOrdering extends Ordering<OrientedDimension3d> {
		public int compare(OrientedDimension3d left, OrientedDimension3d right) {
			return Ints.compare(left.height, right.height);
		}
	}

	private static final class MaximumDimensionOrdering extends Ordering<OrientedDimension3d> {
		@Override
		public int compare(OrientedDimension3d left, OrientedDimension3d right) {
			return Ints.compare(left.base.large, right.base.large);
		}
	}

	private static final class AreaOrdering extends Ordering<OrientedDimension3d> {
		@Override
		public int compare(OrientedDimension3d left, OrientedDimension3d right) {
			return Ints.compare(left.base.area(), right.base.area());
		}
	}

	private static final class PerimeterOrdering extends Ordering<OrientedDimension3d> {
		@Override
		public int compare(OrientedDimension3d left, OrientedDimension3d right) {
			return Ints.compare(left.base.perimeter(), right.base.perimeter());
		}
	}

	public static final Ordering<OrientedDimension3d> AREA_ORDERING = new AreaOrdering();
	public static final Ordering<OrientedDimension3d> MAXIMUM_DIMENSION_ORDERING = new MaximumDimensionOrdering();
	public static final Ordering<OrientedDimension3d> PERIMETER_ORDERING = new PerimeterOrdering();

	private IntervalSlice currentSlice = IntervalSlice.empty(1000, 1000);
	private PerimeterSlice perimeterSlice = PerimeterSlice.empty(1000, 1000);
	private HeightSlice heightSlice = HeightSlice.empty(1000, 1000);

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
				Positioning bestInsertionPoint = bestPositioning(base, fittingRectangles, orientedDimension3d.height);

				this.fillSlices(bestInsertionPoint.point2d, bestInsertionPoint.orientation, orientedDimension3d.height);
				result.add(new PutRectangle(new Rectangle(bestInsertionPoint.point2d, bestInsertionPoint.orientation),
						orientedDimension3d.height));
			} else {
				return result;
			}

		}
		return result;
	}

	private Positioning bestPositioning(final Dimension2d base, Collection<MaximumRectangle> fittingRectangles, int height) {
		List<Positioning> positionings = Lists.newArrayList();
		for (MaximumRectangle maximumRectangle : fittingRectangles) {
			Box2d vertical = base.vertical();
			Rectangle rectangle = maximumRectangle.rectangle;
			if (rectangle.getBox2d().contains(vertical)) {
				addOrientedInsertionPoints(positionings, rectangle, vertical, height);
			}
			Box2d horizontal = base.horizontal();
			if (maximumRectangle.rectangle.getBox2d().contains(horizontal) && !vertical.contains(horizontal)) {
				addOrientedInsertionPoints(positionings, rectangle, horizontal, height);

			}
		}
		Positioning bestInsertionPoint = bestInsertionPoint(positionings);
		return bestInsertionPoint;
	}

	public Positioning bestInsertionPoint(List<Positioning> positionings) {
		Ordering<Positioning> insertionPointOrdering = insertionPointOrdering();
		Positioning bestPositioning = positionings.get(0);
		for (Positioning positioning : positionings) {
			if (insertionPointOrdering.compare(positioning, bestPositioning) > 0) {
				bestPositioning = positioning;
			}
		}
		return bestPositioning;
	}

	public Ordering<Positioning> insertionPointOrdering() {
		// return new SameHeightPositioning().compound(new
		// BestHeightPositioning()).compound(new PerimeterPositioning());
		// return new PerimeterPositioning();
		// return new MixedPositioning();
		// return new BoundariesSidesPositioning().reverse().compound(new
		// PerimeterPositioning());
		return new PerimeterPositioning().compound(new BoundariesSidesPositioning().reverse());
	}

	public List<OrientedDimension3d> orderDimensions(Collection<OrientedDimension3d> dimensions) {
		List<OrientedDimension3d> sortedDimensions = this.getDimensionsOrdering().reverse().sortedCopy(dimensions);
		return sortedDimensions;
	}

	public void fillSlices(Point2d point2d, Box2d orientation, int height) {
		assert this.currentSlice.isFree(point2d.x, point2d.y, orientation.dx, orientation.dy);
		fillMainSlice(point2d, orientation);
		fillPerimeterSlice(point2d, orientation);
		fillHeightSlice(point2d, orientation, height);
	}

	private void freeSlices(Point2d point2d, Box2d box2d) {
		freeMainSlice(point2d, box2d);
		freePerimeterSlice(point2d, box2d);
		freeHeightSlice(point2d, box2d);
	}

	public void fillPerimeterSlice(Point2d point2d, Box2d orientation) {
		this.perimeterSlice.fill(point2d.x, point2d.y, orientation.dx, orientation.dy);
	}

	public void fillHeightSlice(Point2d point2d, Box2d orientation, int height) {
		this.heightSlice.fill(point2d.x, point2d.y, orientation.dx, orientation.dy, height);
	}

	public void freeHeightSlice(Point2d point2d, Box2d orientation) {
		this.heightSlice.free(point2d.x, point2d.y, orientation.dx, orientation.dy);
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

	private void addOrientedInsertionPoints(List<Positioning> positionings, Rectangle rectangle, Box2d vertical, int height) {
		addInsertionPoint(positionings, rectangle.upperLeft(), vertical, height);
		if (rectangle.getBox2d().dx != vertical.dx) {
			addInsertionPoint(positionings, rectangle.upperRight(vertical), vertical, height);
		}
		if (rectangle.getBox2d().dy != vertical.dy) {
			addInsertionPoint(positionings, rectangle.bottomLeft(vertical), vertical, height);
			if (rectangle.getBox2d().dx != vertical.dx) {
				addInsertionPoint(positionings, rectangle.bottomRight(vertical), vertical, height);
			}
		}
	}

	private boolean addInsertionPoint(List<Positioning> positionings, Point2d point2d, Box2d vertical, int height) {

		Interval verticalRange = Interval.of(point2d.y, point2d.y + vertical.dy);
		Interval horizontalRange = Interval.of(point2d.x, point2d.x + vertical.dx);
		//
		// Multiset<Integer> leftSideInfo =
		// this.heightSlice.getLeftSides().getSideInfo(horizontalRange.getFrom(),
		// verticalRange);
		// Multiset<Integer> rightSideInfo =
		// this.heightSlice.getRightSides().getSideInfo(horizontalRange.getTo(),
		// verticalRange);
		//
		// Multiset<Integer> upSideInfo =
		// this.heightSlice.getUpSides().getSideInfo(verticalRange.getFrom(),
		// horizontalRange);
		// Multiset<Integer> downSideInfo =
		// this.heightSlice.getDownSides().getSideInfo(verticalRange.getTo(),
		// horizontalRange);
		//
		// HeightMisfit leftHeightMisfit = misfit(leftSideInfo, height,
		// verticalRange.length());
		// HeightMisfit rightHeightMisfit = misfit(rightSideInfo, height,
		// verticalRange.length());
		// HeightMisfit upHeightMisfit = misfit(upSideInfo, height,
		// horizontalRange.length());
		// HeightMisfit downHeightMisfit = misfit(downSideInfo, height,
		// horizontalRange.length());
		// HeightMisfit heightMisfit = new HeightMisfit(leftHeightMisfit.length
		// + rightHeightMisfit.length
		// + upHeightMisfit.length + downHeightMisfit.length,
		// leftHeightMisfit.same + rightHeightMisfit.same
		// + upHeightMisfit.same + downHeightMisfit.same,
		// leftHeightMisfit.higher + rightHeightMisfit.higher
		// + upHeightMisfit.higher + downHeightMisfit.higher,
		// leftHeightMisfit.lower + rightHeightMisfit.lower
		// + upHeightMisfit.lower + downHeightMisfit.lower,
		// leftHeightMisfit.none + rightHeightMisfit.none
		// + upHeightMisfit.none + downHeightMisfit.none);

		IntRectangle boundaries = new IntRectangle(point2d.x == 0 ? vertical.dy : 0,
				point2d.x + vertical.dx == 0 ? vertical.dy : 0, point2d.y == 0 ? vertical.dx : 0,
				point2d.y + vertical.dy == 0 ? vertical.dx : 0);

		int perimeter = getPerimeter(point2d, vertical);
		return positionings.add(new Positioning(point2d, vertical, perimeter, null, boundaries));
		// return positionings.add(new Positioning(point2d, vertical, perimeter,
		// heightMisfit));
	}

	private HeightMisfit misfit(Multiset<Integer> sideInfo, int height, int length) {
		int higher = 0;
		int lower = 0;
		int same = 0;
		int none = length;
		Set<Entry<Integer>> entrySet = sideInfo.entrySet();
		for (Entry<Integer> entry : entrySet) {
			int element = entry.getElement();
			int count = entry.getCount();
			if (element > height) {
				higher += count * (element - height);
			} else if (element < height) {
				lower += count * (height - element);
			} else {
				same += count;
			}
			if (element != 0) {
				none -= count;
			}
		}
		assert none >= 0;
		return new HeightMisfit(length, same, higher, lower, none);
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
		List<MaximumRectangle> list = Lists.newArrayList();
		for (MaximumRectangle maximumRectangle : maximumRectangles) {
			if (maximumRectangle.rectangle.getBox2d().dimension().contains(base)) {
				list.add(maximumRectangle);
			}
		}
		return list;
	}

	protected Ordering<OrientedDimension3d> getDimensionsOrdering() {
		return AREA_ORDERING;
		// return new MaximumDimensionOrdering();
		// return new HeightOrdering().compound(new AreaOrdering());
	}

	public int getPerimeter(Point2d point2d, Box2d box2d) {
		return this.perimeterSlice.getPerimeterInt(point2d, box2d);
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

	@Override
	public void parasite(Packer packer) {
		IntervalPacker intervalPacker = (IntervalPacker) packer;
		this.currentSlice = intervalPacker.currentSlice.copy();
		this.heightSlice = intervalPacker.heightSlice.copy();
		this.perimeterSlice = intervalPacker.perimeterSlice.copy();
	}

	@Override
	public String toString() {
		return "IntervalPacker[AREA]";
	}
}
