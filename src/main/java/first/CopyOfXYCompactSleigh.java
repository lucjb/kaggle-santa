package first;

import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class CopyOfXYCompactSleigh {

	int currentZ = 1;
	int nextZ = 0;
	int maxZ = 0;
	BitSet topSurface = new BitSet(1000 * 1000);

	private int thereIsRoomFor(int x, int y, int xSize, int ySize) {
		for (int q = y; q < y + ySize; q++) {
			for (int p = x; p < x + xSize; p++) {
				int bitIndex = p * 1000 + q;
				if (topSurface.get(bitIndex)) {
					while (p < 1000 + xSize && topSurface.get(p * 1000 + q)) {
						p++;
					}
					return p;
				}
			}
		}
		return x;
	}

	public void addPresents(List<Present> presents) {
		for (Present present : presents) {
			present.leastSupRotation();
		}
		//addPresentsOrdering(presents);
		int j = 0;
		for (Present present : presents) {
			if (++j % 10000 == 0)
				System.out.println(present.order);
			add(present);
//			 if (!add(present))
//				 return;
		}

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ
						- present.boundaries.get(i).z + 1;
			}
		}
	}

	private List<Present> sort(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private void undoLayer(List<Present> layer) {
		for (Present present : layer) {
			present.boundaries.clear();
		}
		topSurface.clear();
		nextZ = 0;
		maxZ = 0;
	}

	private boolean add(Present present) {
		present.leastSupRotation();
		Point insertPoint = placeFor(present);
		if (insertPoint == null) {
			present.rotate();
			insertPoint = placeFor(present);
		}
		if (insertPoint != null) {
			insert(present, insertPoint);
			return true;
		} else {
			startLayer();
			insert(present, new Point(1, 1, currentZ));
			return false;
		}
	}

	private void startLayer() {
		currentZ = nextZ;
		nextZ = 0;
		topSurface.clear();
	}

	private void insert(Present present, Point insertPoint) {
		this.occupy(insertPoint.x, insertPoint.y, present.xSize, present.ySize);
		
//		System.out.println(present.order + " [" + present.xSize + ","+ present.ySize +","+ present.zSize +"] "+ new Point(insertPoint.x, insertPoint.y,
//				insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y,
				insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y
				+ present.ySize - 1, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y + present.ySize - 1, insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y,
				insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y
				+ present.ySize - 1, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y + present.ySize - 1, insertPoint.z
						+ present.zSize - 1));

		int zp = present.maxZ();
		if (zp > maxZ) {
			maxZ = zp;
		}
		if (zp >= nextZ) {
			nextZ = zp + 1;
		}
	}

	private Point placeFor(Present present) {
		int xSize = present.xSize;
		int ySize = present.ySize;
		for (int yi = 0; yi <= 1000 - ySize;) {
			for (int xi = 0; xi <= 1000 - xSize; xi++) {
				int skip = this.thereIsRoomFor(xi, yi, xSize, ySize);
				if (skip == xi) {
					return new Point(xi + 1, yi + 1, this.currentZ);
				} else {
					xi = skip - 1;
				}
			}
			yi += 1;
		}
		return null;
	}

	private void occupy(int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				topSurface.set((p - 1) * 1000 + (q - 1));
			}
		}
	}

}
