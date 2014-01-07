package pipi.interval;

import java.util.Collection;
import java.util.List;

import pipi.Box2d;
import pipi.Dimension2d;
import pipi.OutputPresent;
import pipi.Point2d;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class IntervalPacker {
	private IntervalSlice currentSlice = IntervalSlice.empty(1000, 1000);
	private IntervalSlice perimeterSlice = IntervalSlice.empty(1000, 1000);

	private int currentZ;
	private int lastZ = 0;
	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalPacker() {
		this.currentZ = 0;
	}

	public List<Rectangle> packPesents(Collection<Dimension2d> dimensions) {
		Ordering<Dimension2d> dimensionsOrdering = getDimensionsOrdering();
		List<Dimension2d> sortedDimensions = dimensionsOrdering.reverse().sortedCopy(dimensions);
		List<Rectangle> result = Lists.newArrayList();

		for (Dimension2d dimension2d : sortedDimensions) {
			final Dimension2d base = dimension2d;
			Collection<MaximumRectangle> maximumRectangles = this.currentSlice.getMaximumRectangles();
			Collection<MaximumRectangle> fittingRectangles = fittingRectangles(maximumRectangles, base);

			if (!fittingRectangles.isEmpty()) {
				Rectangle bestRectangle = bestRectangle(fittingRectangles);
				Box2d orientation;
				if (bestRectangle.getBox2d().contains(base.vertical())) {
					orientation = base.vertical();
				} else {
					orientation = base.horizontal();
				}

				assert this.currentSlice.isFree(bestRectangle.point2d.x, bestRectangle.point2d.y, orientation.dx,
						orientation.dy);
				this.currentSlice.fill(bestRectangle.point2d.x, bestRectangle.point2d.y, orientation.dx, orientation.dy);
				this.perimeterSlice.fill(bestRectangle.point2d.y, bestRectangle.point2d.x, orientation.dy, orientation.dx);
				result.add(new Rectangle(bestRectangle.point2d, orientation));
			} else {
				return result;
			}
		}
		return result;
	}

	private Rectangle bestRectangle(Collection<MaximumRectangle> fittingRectangles) {
		return new Ordering<MaximumRectangle>() {

			@Override
			public int compare(MaximumRectangle left, MaximumRectangle right) {
				return Ints.compare(left.rectangle.getBox2d().area(), right.rectangle.getBox2d().area());
			}
		}.min(fittingRectangles).rectangle;
	}

	private Collection<MaximumRectangle> fittingRectangles(Collection<MaximumRectangle> maximumRectangles, final Dimension2d base) {
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
	
	public Perimeter getPerimeter(Point2d point2d, Box2d box2d){
		return this.currentSlice.getPerimeter(point2d, box2d, this.perimeterSlice);
	}
}
