package first;

import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

/**
 * 5,270,836
 * 
 * @author lbernardi
 * 
 */
public class NaiveSleigh {

	int currentZ = 1;
	int currentX = 1;
	int currentY = 1;

	int nextY = 0;
	int nextZ = 0;
	int maxZ = 0;

	BitSet topSurface = new BitSet(1000 * 1000);

	public int fit(int x, int y, int xSize, int ySize) {
		if (x + xSize > 1000 || y + ySize > 1000)
			return 1000;
		BitSet currentLayer = topSurface;
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				int bitIndex = p * 1000 + q;
				if (!currentLayer.get(bitIndex)) {
					int nextSetBit = currentLayer.nextSetBit(bitIndex + 1);
					if (nextSetBit < 0 || nextSetBit / 1000 != p) {
						return 1000;
					} else {
						return nextSetBit % 1000;
					}
				}
			}
		}
		return y;
	}

	public void addPresents(List<Present> presents) {
		int k = 0;
		List<Present> layer = Lists.newArrayList();
		for (Present present : presents) {
			if (!add(present)) {
				undoLayer(layer);
				layer.add(present);
				List<Present> sortedLayer;
				sortedLayer = sortY(layer);

				if (reinsert(sortedLayer)) {
					layer = sortedLayer;
				} else {
					undoLayer(sortedLayer);
					layer.remove(present);
					if (!reinsert(layer)) {
						System.err.println("Wrong!");
					}
					startLayer();
					layer.clear();
					add(present);
					layer.add(present);
				}
			} else {
				layer.add(present);
			}

			if (k % 10000 == 0) {
				System.out.println(k + "z= " + currentZ);
			}
			k++;

		}

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = maxZ - present.boundaries.get(i).z + 1;
			}
		}
	}

	private void startLayer() {
		pushCurrentZ();
		topSurface.set(0, 1000 * 1000);
	}

	private Point firstFittingPoint(Present present) {
		for (int xi = 0; xi <= 999;) {
			for (int yi = 0; yi <= 999;) {
				int skip = checkBothOrientations(xi, yi, present);
				if (skip == yi) {
					return new Point(xi + 1, yi + 1, currentZ);
				} else {
					yi = skip;
				}
			}
			xi += 1;
		}

		return null;
	}

	private boolean reinsert(List<Present> presents) {
		for (Present p : presents) {
			if (!add(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean encajate(Present present) {
		Point ip = firstFittingPoint(present);
		if (ip == null) {
			present.rotateMedMaxMin();
			ip = firstFittingPoint(present);
		}
		if (ip != null) {
			int zPut = currentZ;
			present.boundaries.add(new Point(ip.x, ip.y, zPut));
			present.boundaries.add(new Point(ip.x, ip.y + present.ySize - 1, zPut));
			present.boundaries.add(new Point(ip.x + present.xSize - 1, ip.y, zPut));
			present.boundaries.add(new Point(ip.x + present.xSize - 1, ip.y + present.ySize - 1, zPut));

			present.boundaries.add(new Point(ip.x, ip.y, zPut + present.zSize - 1));
			present.boundaries.add(new Point(ip.x, ip.y + present.ySize - 1, zPut + present.zSize - 1));
			present.boundaries.add(new Point(ip.x + present.xSize - 1, ip.y, zPut + present.zSize - 1));
			present.boundaries.add(new Point(ip.x + present.xSize - 1, ip.y + present.ySize - 1, zPut + present.zSize - 1));

			unset(ip.x - 1, ip.y - 1, present.xSize, present.ySize);
			if (ip.x + present.xSize - 1 > currentX && ip.y + present.ySize - 1 > currentY) {
				currentX = ip.x + present.xSize - 1;
				if (ip.y + present.ySize - 1 > nextY) {
					nextY = ip.y + present.ySize - 1;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private List<Present> sortSupX(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
				if (compare == 0)
					return -Ints.compare(o1.xSize, o2.xSize);
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private void undoLayer(List<Present> layer) {
		for (Present present : layer) {
			present.boundaries.clear();
			present.rotateMinMedMax();
		}
		currentX = 1;
		currentY = 1;
		nextY = 0;
		nextZ = 0;
		maxZ = 0;
		topSurface.set(0, 1000 * 1000);
	}

	private boolean add(Present present) {
		present.rotateMinMedMax();

		if (!fitsX(present)) {
			pushCurrentY();
		}
		if (!fitsY(present)) {
			return encajate(present);
		}
		insert(present);
		return true;
	}

	private List<Present> sortX(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.xSize, o2.xSize);
				if (compare == 0) {
					return -Ints.compare(o1.ySize, o2.ySize);
				}
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private List<Present> sortY(List<Present> layer) {
		List<Present> sortedCopy = Ordering.from(new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				int compare = -Ints.compare(o1.ySize, o2.ySize);
				if (compare == 0) {
					return -Ints.compare(o1.xSize, o2.xSize);
				}
				return compare;
			}
		}).sortedCopy(layer);
		return sortedCopy;
	}

	private int checkBothOrientations(int x, int y, Present present) {
		int fit = fit(x, y, present.xSize, present.ySize);
		if (fit != y) {
			int sfit = fit(x, y, present.ySize, present.xSize);
			if (sfit != y) {
				return fit;
			} else {
				int aux = present.xSize;
				present.xSize = present.ySize;
				present.ySize = aux;
				present.computeMinMedMax();
				return y;
			}
		}
		return fit;
	}

	private void insert(Present present) {
		int zPut = currentZ;
		present.boundaries.add(new Point(currentX, currentY, zPut));
		present.boundaries.add(new Point(currentX, currentY + present.ySize - 1, zPut));
		present.boundaries.add(new Point(currentX + present.xSize - 1, currentY, zPut));
		present.boundaries.add(new Point(currentX + present.xSize - 1, currentY + present.ySize - 1, zPut));

		present.boundaries.add(new Point(currentX, currentY, zPut + present.zSize - 1));
		present.boundaries.add(new Point(currentX, currentY + present.ySize - 1, zPut + present.zSize - 1));
		present.boundaries.add(new Point(currentX + present.xSize - 1, currentY, zPut + present.zSize - 1));
		present.boundaries.add(new Point(currentX + present.xSize - 1, currentY + present.ySize - 1, zPut + present.zSize
				- 1));

		unset(currentX - 1, currentY - 1, present.xSize, present.ySize);

		currentX = currentX + present.xSize;
		pushNextY(present.ySize);
		pushNextZ(present);
		int z = present.maxZ();
		if (z > maxZ) {
			maxZ = z;
		}
	}

	private void unset(int x, int y, int xSize, int ySize) {
		for (int xi = x; xi < x + xSize; xi++) {
			topSurface.clear(xi * 1000 + y, xi * 1000 + y + ySize);
		}
	}

	private void set(int x, int y, int xSize, int ySize) {
		for (int xi = x; xi < x + xSize; xi++) {
			topSurface.set(xi * 1000 + y, xi * 1000 + y + ySize);
		}
	}

	private boolean fitsY(Present present) {
		return !(currentY + present.ySize > 1000 + 1);
	}

	private boolean fitsX(Present present) {
		return !(currentX + present.xSize > 1000 + 1);
	}

	private void pushCurrentZ() {
		currentX = 1;
		currentY = 1;
		currentZ = nextZ;
		nextZ = 0;
		nextY = 0;
	}

	private void pushCurrentY() {
		currentX = 1;
		currentY = nextY;
		nextY = 0;
	}

	private void pushNextZ(Present present) {
		if (present.maxZ() > nextZ) {
			nextZ = present.maxZ() + 1;
		}
	}

	private void pushNextY(int ySize) {
		if (currentY + ySize > nextY) {
			nextY = currentY + ySize;
		}
	}
}
