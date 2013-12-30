package pipi;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

import first.Point;

public class Main {

	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("presents.csv");
		IntervalSleigh superSleigh = new IntervalSleigh();

		RateLimiter rateLimiter = RateLimiter.create(1.0);
		int count = 0;
		List<OutputPresent> outputPresents = Lists.newArrayList();
		for (SuperPresent superPresent : presents) {
			Dimension3d dimension = superPresent.getDimension();
			Box3d box3d = new Box3d(dimension.small, dimension.medium, dimension.large);

			int order = superPresent.getOrder();
			Point point = superSleigh.putPesent(box3d);
			outputPresents.add(new OutputPresent(order, point, box3d));
			count++;
			if (rateLimiter.tryAcquire()) {
				System.out.printf("Z: %d\n", superSleigh.getCurrentZ());
				System.out.printf("Progress: %d\n", count);
			}
		}
		OutputPresent.outputPresents(outputPresents, superSleigh.getLastZ(), "intervals.csv");
	}

}
