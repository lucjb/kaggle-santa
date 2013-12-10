import java.util.BitSet;
import java.util.List;

import com.google.common.collect.Lists;

public class BitsetsSleigh {

	List<BitSet> space = Lists.newArrayList();
	List<Integer> freePointsCount = Lists.newArrayList();
	List<Integer> firstFreePoints = Lists.newArrayList();

	public void addPresent(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		int zSize = present.zSize;
		int sup = xSize * ySize;

		int z = 0;
		while (true) {
			if (z == this.space.size()) {
				this.space.add(new BitSet(1000000));
				this.freePointsCount.add(1000 * 1000);
				this.firstFreePoints.add(0);
			}
			if (freePointsCount.get(z) < sup) {
				z++;
				continue;
			}
			Integer ffp = firstFreePoints.get(z);
			int ffx = ffp / 1000;
			int ffy = ffp % 1000;

			for (int x = ffx; x < 1000 - xSize;) {
				for (int y = ffy; y < 1000 - ySize; y++) {
					int skip = this.thereIsRoomFor(x, y, z, xSize, ySize, zSize);
					if (skip == y) {
						System.out.println(present + ": " + x + " " + y + " " + z);
						this.occupy(x, y, z, xSize, ySize, zSize);
						return;
					} else {
						y = skip - 1;
					}
				}
				x += 10;
			}
			z++;
		}

	}

	private int thereIsRoomFor(int x, int y, int z, int xSize, int ySize, int zSize) {
		for (int r = z; r < z + zSize; r++) {
			if (r == this.space.size()) {
				this.space.add(new BitSet(1000000));
				this.freePointsCount.add(1000 * 1000);
				this.firstFreePoints.add(0);
			} else {
				BitSet slice = this.space.get(r);
				for (int p = x; p < x + xSize; p++) {
					for (int q = y; q < y + ySize; q++) {
						if (slice.get(p * 1000 + q)) {
							return q + 1;
						}
					}
				}
			}
		}
		return y;
	}

	private void occupy(int x, int y, int z, int xSize, int ySize, int zSize) {
		for (int r = z; r < z + zSize; r++) {
			BitSet slice = this.space.get(r);
			Integer ffp = firstFreePoints.get(z);
			int ffx = ffp / 1000;
			int ffy = ffp % 1000;
			boolean ffpChanged = false;
			freePointsCount.set(r, freePointsCount.get(r) - xSize * ySize);
			for (int p = x; p < x + xSize; p++) {
				for (int q = y; q < y + ySize; q++) {
					slice.set(p * 1000 + q);
					if (p == ffx && q == ffy) {
						ffpChanged = false;
					}
				}
			}
			if (ffpChanged) {
				for (int p = ffx; p < 1000; p++) {
					for (int q = ffy; q < 1000; q++) {
						if (!slice.get(p * 1000 + q)) {
							ffp = ffx * 1000 + ffy;
							firstFreePoints.set(r, ffp);
						}
					}
				}
			}
		}
	}
}
