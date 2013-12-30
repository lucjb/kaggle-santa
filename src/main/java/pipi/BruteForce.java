package pipi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import pipi.bitmatrix.BitsetSlice;
import pipi.interval.IntervalSlice;
import pipi.interval.MaximumRectangle;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

public class BruteForce {
	static int size = 100;
	public static void main(String[] args) {
		// List<Box2d> boxes = Arrays.asList(new Box2d(3, 2), new Box2d(4, 1),
		// new Box2d(2, 1));
		 List<Box2d> boxes = Arrays.asList(new Box2d(20, 5), new Box2d(10,
		 10), new Box2d(5, 20));
//		List<Box2d> boxes = Arrays.asList(new Box2d(998, 993), new Box2d(991, 995), new Box2d(999, 995));
		int[] xs = new int[boxes.size()];
		int[] ys = new int[boxes.size()];
		long count=0;
		long negative = 0;
		do {
			// for (int i = 0; i < boxes.size(); i++) {
			// System.out.printf("(%d, %d) ", xs[i], ys[i]);
			// }
			// System.out.println();
			BitsetSlice bitsetSlice = BitsetSlice.freed(size);
			IntervalSlice intervalSlice = IntervalSlice.empty(size, size);
			for (int i = 0; i < xs.length; i++) {
				int x = xs[i];
				int y = ys[i];
				Box2d box2d = boxes.get(i);
				bitsetSlice.fill(x, y, box2d.dx, box2d.dy);
				intervalSlice.fill(x, y, box2d.dx, box2d.dy);
			}
			BitsetSlice validationSleighSlice = BitsetSlice.filled(size);
			Collection<MaximumRectangle> maximumRectangles = intervalSlice.getMaximumRectangles();
			for (MaximumRectangle maximumRectangle : maximumRectangles) {
				validationSleighSlice.free(maximumRectangle.getHorizontalRange().getFrom(), maximumRectangle.getVerticalRange().getFrom(),
						maximumRectangle.getHorizontalRange().length(), maximumRectangle.getVerticalRange().length());
			}
			if(!validationSleighSlice.equals(bitsetSlice)){
				System.out.println("--NEGATIVE--");
				for (int i = 0; i < xs.length; i++) {
					int x = xs[i];
					int y = ys[i];
					Box2d box2d = boxes.get(i);
					System.out.printf("%d,%d,%d,%d\n",x, y, box2d.dx, box2d.dy);
				}
				negative++;
				return;
			}
			count++;
		} while (increment(xs, ys, boxes));
		System.out.println("TOTAL: "+count);
		System.out.println("NEGATIVE: "+negative);

	}

	private static boolean increment(int[] xs, int[] ys, List<Box2d> boxes) {
		for (int i = xs.length - 1; i >= 0; i--) {
			ys[i]++;
			if (ys[i] > size - boxes.get(i).dy) {
				ys[i] = 0;
				xs[i]++;
				if (xs[i] > size - boxes.get(i).dx) {
					xs[i] = 0;
					continue;
				}
			}
			return true;
		}
		return false;
	}
}
