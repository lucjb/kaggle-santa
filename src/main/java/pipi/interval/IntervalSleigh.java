package pipi.interval;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import pipi.Box2d;
import pipi.Box3d;
import pipi.Dimension2d;
import pipi.Dimension3d;
import pipi.OutputPresent;
import pipi.SuperPresent;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import first.Point;

public class IntervalSleigh {
	private IntervalSlice currentSlice = IntervalSlice.empty(1000, 1000);

	private int currentZ;
	private int lastZ = 0;
	private Multiset<Integer> currentZs = HashMultiset.create();
	private int count = 0;
	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalSleigh() {
		this.currentZ = 0;
	}

	public void putPesent(SuperPresent superPresent) {

		Dimension3d dimension = superPresent.getDimension();
		final Dimension2d base = dimension.smallFace(); 
		int dz = dimension.large;
//		final Box3d box3d = new Box3d(dimension.small, superPresent.getDimension().medium, superPresent.getDimension().large);
		for (;;) {
			Collection<Rectangle> maximumRectangles = this.currentSlice.getMaximumRectangles();
			Collection<Rectangle> filter = Collections2.filter(maximumRectangles, new Predicate<Rectangle>() {

				@Override
				public boolean apply(Rectangle rectangle) {
					return rectangle.getBox2d().dimension().contains(base);
				}
			});

			if (!filter.isEmpty()) {
				Rectangle min = new Ordering<Rectangle>() {

					@Override
					public int compare(Rectangle left, Rectangle right) {
						return Ints.compare(left.getBox2d().area(), right.getBox2d().area());
					}
				}.min(filter);
				Box2d orientation;
				if(min.getBox2d().contains(base.vertical())){
					orientation = base.vertical();
				}else{
					orientation=base.horizontal();
				}
				
				this.currentSlice.fill(min.getPoint2d().getX(), min.getPoint2d().getY(), orientation.dx,
						orientation.dy);
				this.count++;
				int newZ = this.currentZ + dz;
				this.currentZs.add(dz, orientation.area());
				this.lastZ = Math.max(this.lastZ, newZ);

				Box3d box3d = new Box3d(orientation.dx,orientation.dy, dz);
				this.outputPresents.add(new OutputPresent(superPresent.getOrder(), new Point(min.getPoint2d().getX(), min.getPoint2d().getY(), this.currentZ), box3d));
				return ;
			} else {
				int nextZ = this.lastZ;
				int deltaZ = nextZ - this.currentZ;
				int waste2d = 1000000 - this.currentZs.size();
				long waste = 0;
				Set<Entry<Integer>> entrySet = this.currentZs.entrySet();
				for (Entry<Integer> entry : entrySet) {
					waste += (deltaZ - entry.getElement()) * entry.getCount();
				}
				waste += waste2d * deltaZ;
//				System.out.println("Present before: " + this.count);
//				System.out.println("Waste: " + waste);
//				System.out.println("2D waste: " + waste2d);
				this.currentZ = nextZ;
				this.currentSlice = IntervalSlice.empty(1000, 1000);
				this.currentZs.clear();
			}
		}
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
}
