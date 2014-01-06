package pipi.main;

import java.util.List;

import pipi.OutputPresent;
import pipi.SuperPresent;
import pipi.SuperPresentsParser;
import pipi.interval.IntervalPacker;
import pipi.interval.IntervalSleigh;

import com.google.common.util.concurrent.RateLimiter;

public class Main {

	public static void main(String[] args) throws Exception {
		List<SuperPresent> presents = new SuperPresentsParser().parse("presents.csv");
		RateLimiter rateLimiter = RateLimiter.create(0.1);
		int count = 0;

		IntervalPacker packer = new IntervalPacker();
		for (int i = 0; i < presents.size();) {
			int saturateAreaSmall = saturateAreaSmall(presents, i, 1000 * 1000);
			int saturateAreaLarge = saturateAreaLarge(presents, i, 1000 * 1000);
			System.out.println(saturateAreaSmall);
			System.out.println(saturateAreaLarge);
			
			System.exit(0);
		}
		
		for (SuperPresent superPresent : presents) {
			packer.putPesent(superPresent);
			// System.out.printf("%d, %d, %d, %d\n", point.x, point.y, box3d.dx,
			// box3d.dy);
			count++;
			if (rateLimiter.tryAcquire()) {
				System.out.printf("Z: %d\n", packer.getCurrentZ());
				System.out.printf("Progress: %d\n", count);
			}
		}
		int maximumZ = packer.getLastZ();
		OutputPresent.outputPresents(packer.getOutputPresents(), maximumZ, "intervals.csv");
		System.out.println(maximumZ * 2);
	}

	private static int saturateAreaSmall(List<SuperPresent> presents, int start, int leftArea) {
		int j;
		for (j = start; j < presents.size(); j++) {
			SuperPresent superPresent = presents.get(j);
			int area = superPresent.getDimension().smallFace().area();
			int newArea = leftArea - area;
			if (newArea < 0) {
				break;
			}
			leftArea = newArea;
		}
		return j;
	}

	private static int saturateAreaLarge(List<SuperPresent> presents, int start, int leftArea) {
		int j;
		for (j = start; j < presents.size(); j++) {
			SuperPresent superPresent = presents.get(j);
			int area = superPresent.getDimension().largeFace().area();
			int newArea = leftArea - area;
			if (newArea < 0) {
				break;
			}
			leftArea = newArea;
		}
		return j;
	}

}
