package pipi.main;

import java.util.List;

import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.interval.IntervalSleigh;

public class Profile {
	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("prof.csv");
		IntervalSleigh superSleigh = new IntervalSleigh();

		long start = System.currentTimeMillis();
		long count = 0;
		for (int i = 0; i < 1; i++) {
			for (SuperPresent superPresent : presents) {
				superSleigh.putPesent(superPresent);
				count++;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start) / (double) count);
	}

}
