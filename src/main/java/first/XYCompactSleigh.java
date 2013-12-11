package first;

import java.util.BitSet;
import java.util.List;

public class XYCompactSleigh {

	int currentZ = 1;
	int nextZ = 0;
	int maxZ = 0;
	BitSet topSurface = new BitSet(1000 * 1000);

	private int thereIsRoomFor(int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				int bitIndex = p * 1000 + q;
				if (topSurface.get(bitIndex)) {
					// return topSurface.nextClearBit(bitIndex);
					while (q < 1000 + ySize && topSurface.get(p * 1000 + q)) {
						q++;
					}
					return q;
				}
			}
		}
		return y;
	}

	public void addPresents(List<Present> presents) {
		int u = 0;
		for (Present present : presents) {
			present.leastSupRotation();
			add(present);
			if (u % 1000 == 0)
				System.out.println(present);
			u++;
		}

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ - present.boundaries.get(i).z + 1;
			}
		}
	}

	private boolean add(Present present) {
		int x = 1;
		int y = 1;
		int z = currentZ;

		Point fit = put(present);
		if (fit == null) {
			present.rotate();
			fit = put(present);
		}
		if (fit == null) {
			currentZ = nextZ;
			nextZ = 0;
			z = currentZ;
			topSurface.clear();
			occupy(x, y, present.xSize, present.ySize);
		} else {
			x = fit.x;
			y = fit.y;
		}
		present.boundaries.add(new Point(x, y, z));
		present.boundaries.add(new Point(x, y + present.ySize - 1, z));
		present.boundaries.add(new Point(x + present.xSize - 1, y, z));
		present.boundaries.add(new Point(x + present.xSize - 1, y + present.ySize - 1, z));

		present.boundaries.add(new Point(x, y, z + present.zSize - 1));
		present.boundaries.add(new Point(x, y + present.ySize - 1, z + present.zSize - 1));
		present.boundaries.add(new Point(x + present.xSize - 1, y, z + present.zSize - 1));
		present.boundaries.add(new Point(x + present.xSize - 1, y + present.ySize - 1, z + present.zSize - 1));

		int zp = present.maxZ();
		if (zp > maxZ) {
			maxZ = zp;
		}
		if (zp >= nextZ) {
			nextZ = zp + 1;
		}
		return true;
	}

	private Point put(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		for (int xi = 0; xi <= 1000 - xSize;) {
			for (int yi = 0; yi <= 1000 - ySize; yi++) {
				int skip = this.thereIsRoomFor(xi, yi, xSize, ySize);
				if (skip == yi) {
					this.occupy(xi, yi, xSize, ySize);
					return new Point(xi + 1, yi + 1, this.currentZ);
				} else {
					yi = skip - 1;
				}
			}
			xi += 1;
		}
		return null;
	}

	private void occupy(int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				topSurface.set(p * 1000 + q);
			}
		}
	}

}
