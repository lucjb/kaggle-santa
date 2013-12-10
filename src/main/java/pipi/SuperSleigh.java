package pipi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.RateLimiter;

import first.Point;

public class SuperSleigh {
	public static void main(String[] args) {
		List<SuperPresent> presents = new SuperPresentsParser().parse("presents.csv");
		SuperSleigh superSleigh = new SuperSleigh();

		RateLimiter rateLimiter = RateLimiter.create(1.0);
		int count = 0;
		try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(Paths.get(".", "super.csv"), Charsets.UTF_8)) {
			for (SuperPresent superPresent : presents) {
				PresentDimension dimension = superPresent.getDimension();
				Box box = new Box(dimension.small, dimension.medium, dimension.large);
				Point point = superSleigh.putPesent(box);
				int order = superPresent.getOrder();
				int[] ouputPresent = ouputPresent(order, point, box);
				newBufferedWriter.write(Joiner.on(',').join(Ints.asList(ouputPresent)));
				newBufferedWriter.newLine();
				count++;
				if (rateLimiter.tryAcquire()) {
					System.out.printf("Progress: %f%%\n", 100.0 * ((double) count / presents.size()));
					System.out.printf("Progress: %d\n", count);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static int[] ouputPresent(int order, Point point, Box box) {
		int[] result = new int[8 * 3 + 1];
		result[0] = order;
		int[] start = new int[] { point.x + 1, point.y + 1, point.z + 1 };
		int[] end = new int[] { start[0] + box.dx - 1, start[1] + box.dy - 1, start[2] + box.dz - 1 };
		int j = 1;
		for (int i = 0; i < 8; i++) {
			for (int d = 0; d < 3; d++) {
				if ((i & (1 << d)) != 0) {
					result[j] = end[d];
				} else {
					result[j] = start[d];
				}
				j++;
			}
		}
		return result;
	}

	public Point putPesent(Box box) {
		Point point = putPoint(box);
		fillZ(point, box);

		return point;
	}

	public Point putPoint(Box box) {
		Point point = new Point(0, 0, Integer.MAX_VALUE);
		for (int x = 0; x + box.dx <= 1000; x++) {
			for (int y = 0; y + box.dy <= 1000; y++) {
				int z = getMaximumZ(x, y, box);
				if (z < point.z) {
					point.x = x;
					point.y = y;
					point.z = z;
				}
			}
		}
		return point;
	}

	private int getMaximumZ(int sx, int sy, Box box) {
		int z = 0;
		for (int x = sx; x < sx + box.dx; x++) {
			for (int y = sy; y < sy + box.dy; y++) {
				z = Math.max(z, this.heightMap[x][y]);
			}
		}
		return z;
	}

	private void fillZ(Point point, Box box) {
		int z = point.z + box.dz;
		for (int x = point.x; x < point.x + box.dx; x++) {
			for (int y = point.y; y < point.y + box.dy; y++) {
				this.heightMap[x][y] = z;
			}
		}
	}

	private int[][] heightMap = new int[1000][1000];
}
