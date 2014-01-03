package pipi;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

import first.Point;

public class Profile {
	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("prof.csv");
		IntervalSleigh superSleigh = new IntervalSleigh();

		long start = System.currentTimeMillis();
		long count = 0;
		List<OutputPresent> outputPresents = Lists.newArrayList();
		for (int i = 0; i < 10; i++) {
			for (SuperPresent superPresent : presents) {
				Dimension3d dimension = superPresent.getDimension();
				Box3d box3d = new Box3d(dimension.small, dimension.medium, dimension.large);

				int order = superPresent.getOrder();
				Point point = superSleigh.putPesent(box3d);
				outputPresents.add(new OutputPresent(order, point, box3d));
				count++;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start) / (double) count);
	}

}
