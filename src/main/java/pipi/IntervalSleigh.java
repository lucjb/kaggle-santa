package pipi;

import java.util.Collection;
import java.util.Set;

import pipi.interval.IntervalSlice;
import pipi.interval.MaximumRectangle;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
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

	public IntervalSleigh() {
		this.currentZ = 0;
	}

	public Point putPesent(final Box3d box3d) {
		for (;;) {
			Collection<MaximumRectangle> maximumRectangles = this.currentSlice.getMaximumRectangles();
			Collection<MaximumRectangle> filter = Collections2.filter(maximumRectangles, new Predicate<MaximumRectangle>() {

				@Override
				public boolean apply(MaximumRectangle input) {
					return input.getHorizontalRange().length() >= box3d.dx && input.getVerticalRange().length() >= box3d.dy;
				}
			});

			if (!filter.isEmpty()) {
				MaximumRectangle min = new Ordering<MaximumRectangle>() {

					@Override
					public int compare(MaximumRectangle left, MaximumRectangle right) {
						return Ints.compare(left.area(), right.area());
					}
				}.min(filter);
				this.currentSlice.fill(min.getHorizontalRange().getFrom(), min.getVerticalRange().getFrom(), box3d.dx,
						box3d.dy);
				this.count++;
				int newZ = this.currentZ + box3d.dz;
				this.currentZs.add(box3d.dz, box3d.dx * box3d.dy);
				this.lastZ = Math.max(this.lastZ, newZ);
				return new Point(min.getHorizontalRange().getFrom(), min.getVerticalRange().getFrom(), this.currentZ);
			} else {
				int nextZ = this.lastZ;
				int deltaZ = nextZ - currentZ;
				int waste2d = 1000000 - this.currentZs.size();
				long waste = 0;
				Set<Entry<Integer>> entrySet = this.currentZs.entrySet();
				for (Entry<Integer> entry : entrySet) {
					waste += (deltaZ - entry.getElement()) * entry.getCount();
				}
				waste += waste2d * deltaZ;
				System.out.println("Present before: " + this.count);
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

}
