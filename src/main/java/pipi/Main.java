package pipi;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

import first.Point;

public class Main {

	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("minip.csv");
		IntervalSliceSleigh superSleigh = new IntervalSliceSleigh();

		RateLimiter rateLimiter = RateLimiter.create(1.0);
		int count = 0;
		List<OutputPresent> outputPresents = Lists.newArrayList();
		for (SuperPresent superPresent : presents) {
			PresentDimension dimension = superPresent.getDimension();
			Box box = new Box(dimension.small, dimension.medium, dimension.large);

			int order = superPresent.getOrder();
			Point point = superSleigh.putPesent(box);
			outputPresents.add(new OutputPresent(order, point, box));
			count++;
			if (rateLimiter.tryAcquire()) {
				System.out.printf("Z: %d\n", superSleigh.getCurrentZ());
				System.out.printf("Progress: %d\n", count);
			}
		}
		OutputPresent.outputPresents(outputPresents, superSleigh.getLastZ(), "intervals.csv");
	}

}
