package pipi.main;

import java.util.List;

import pipi.OutputPresent;
import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.interval.IntervalSleigh;

import com.google.common.util.concurrent.RateLimiter;

public class Main {

	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("presents.csv");
		IntervalSleigh superSleigh = new IntervalSleigh();
		RateLimiter rateLimiter = RateLimiter.create(0.1);
		int count = 0;
		for (SuperPresent superPresent : presents) {
			superSleigh.putPesent(superPresent);
			// System.out.printf("%d, %d, %d, %d\n", point.x, point.y, box3d.dx,
			// box3d.dy);
			count++;
			if (rateLimiter.tryAcquire()) {
				System.out.printf("Z: %d\n", superSleigh.getCurrentZ());
				System.out.printf("Progress: %d\n", count);
			}
		}
		int maximumZ = superSleigh.getLastZ();
		OutputPresent.outputPresents(superSleigh.getOutputPresents(), maximumZ, "intervals.csv");
		System.out.println(maximumZ * 2);
	}

}
