package pipi;

import java.util.Collection;
import java.util.Set;

import pipi.bitmatrix.BitsetSleighSlice;
import pipi.interval.InvervalSleghSlice;
import pipi.interval.Rectangle;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import first.Point;

public class IntervalSliceSleigh {
	private InvervalSleghSlice currentSlice = InvervalSleghSlice.empty(1000, 1000);
	private BitsetSleighSlice currentBackupSlice = BitsetSleighSlice.freed();

	private int currentZ;
	private int lastZ = 0;
	private int sliceCount= 1;
	private Multiset<Integer> currentZs = HashMultiset.create();
	private int count = 0;
	
	public IntervalSliceSleigh() {
		this.currentZ = 0;
	}

	public Point putPesent(final Box box) {
		for (;;) {
			Collection<Rectangle> maximumRectangles = this.currentSlice.getMaximumRectangles();
//			if(count > 0){
//				System.out.println(count + "->" + maximumRectangles);
//			}
//			if(this.count > 364727){
				for (Rectangle rectangle : maximumRectangles) {
				boolean free = this.currentBackupSlice.isFree(rectangle.getHorizontalRange().getFrom(), rectangle.getVerticalRange().getFrom(), rectangle.getHorizontalRange().length(), rectangle.getVerticalRange().length());
				if(!free){
					throw new RuntimeException("la puta madre");
				}
			}
//			}
			Collection<Rectangle> filter = Collections2.filter(maximumRectangles, new Predicate<Rectangle>() {

				@Override
				public boolean apply(Rectangle input) {
					return input.getHorizontalRange().length() >= box.dx && input.getVerticalRange().length() >= box.dy;
				}
			});

			
			if (!filter.isEmpty()) {
				Rectangle min = new Ordering<Rectangle>() {

					@Override
					public int compare(Rectangle left, Rectangle right) {
						return Ints.compare(left.area(), right.area());
					}
				}.min(filter);
				this.currentSlice.fill(min.getHorizontalRange().getFrom(), min.getVerticalRange().getFrom(), box.dx, box.dy);
				this.currentBackupSlice.fill(min.getHorizontalRange().getFrom(), min.getVerticalRange().getFrom(), box.dx, box.dy);
				System.out.printf("%d, %d, %d, %d\n",min.getHorizontalRange().getFrom(), min.getVerticalRange().getFrom(), box.dx, box.dy);
				this.count++;
				int newZ = this.currentZ + box.dz;
				this.currentZs.add(box.dz, box.dx * box.dy);
				this.lastZ = Math.max(this.lastZ, newZ);
				return new Point(min.getHorizontalRange().getFrom(), min.getVerticalRange().getFrom(), this.currentZ);
			} else {
				int nextZ = this.lastZ;
				int deltaZ = nextZ - currentZ;
				int waste2d = 1000000 - this.currentZs.size();
				long waste = 0;
				Set<Entry<Integer>> entrySet = this.currentZs.entrySet();
				for (Entry<Integer> entry : entrySet) {
					waste += (deltaZ - entry.getElement() ) * entry.getCount();
				}
				waste += waste2d * deltaZ;
				System.out.println("Present before" + this.count);
				System.out.println("Waste: " + waste);
				System.out.println("2D waste: " + waste2d);
				this.currentZ = nextZ;
				this.currentSlice = InvervalSleghSlice.empty(1000, 1000);
				this.currentBackupSlice = BitsetSleighSlice.freed();
				this.sliceCount++;
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
