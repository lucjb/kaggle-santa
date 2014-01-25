package first;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class ZCompressor {
	int[][] top = new int[1000][1000];
	int[][] prevTop;
	int minAllowedZ = 1;

	private int[][] cloneTop(int[][] top) {
		int[][] m = new int[top.length][];
		for (int i = 0; i < top.length; i++) {
			int[] aMatrix = top[i];
			int aLength = aMatrix.length;
			m[i] = new int[aLength];
			System.arraycopy(aMatrix, 0, m[i], 0, aLength);
		}
		return m;
	}

	public static void main(String[] args) throws IOException {
		// List<Present> presents = new
		// PresentsParser().parseOutput("areavscolumeNOpushdown.csv");
		List<Present> presents = new PresentsParser().parseOutput("chaia.csv");
		// List<Present> presents = new
		// PresentsParser().parseOutput("testing.csv");

		// List<Present> presents = new
		// PresentsParser().parseOutput("gravitycompressortest.csv");
		new ZCompressor().compress(presents);
		BrunoMain.printEvaluation(presents);
		BrunoMain.generateCSV(presents);
	}

	private void compress(List<Present> presents) {
		List<Present> currentLayer = Lists.newArrayList();
		int currentZ = 1;
		int p = 0;
		for (Present present : presents) {
			Point oneBasedInsertionPoint = present.location();
			if (oneBasedInsertionPoint.z == currentZ) {
				currentLayer.add(present);
				occupy(present, oneBasedInsertionPoint);
			} else {
				if (prevTop != null) {
					pushDown(currentLayer);
				}

				prevTop = cloneTop(top);
				currentLayer.clear();
				currentLayer.add(present);
				currentZ = oneBasedInsertionPoint.z;
				occupy(present, oneBasedInsertionPoint);

			}
			p++;
			if (p % 1000 == 0)
				System.out.println(p);
		}
		pushDown(currentLayer);
		int computeMaxZFromTop = computeMaxZFromTop();
		int zeroBasedMaxOccupiedZ = computeMaxZFromTop;
		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = zeroBasedMaxOccupiedZ + 1 - present.boundaries.get(i).z;
			}
		}

	}

	private List<Present> clonePresents(List<Present> presents) {
		List<Present> presentsClones = new ArrayList<Present>(1000 * 1000);
		for (Present present : presents) {
			try {
				presentsClones.add((Present) present.clone());
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
		return presentsClones;
	}

	private void pushDown(List<Present> layer) {
		List<Present> sortNatural = sortNatural(layer);
		int lastPushDown = Integer.MAX_VALUE;
		for (Present p : sortNatural) {
			if (p.order == 980)
				System.out.println("pushed down");
			if (p.order == 978)
				System.out.println("pushed down");
			Point insertPoint = p.location();
			int maxZBelow = Math.max(maxZBelow(p), minAllowedZ - 1);

			int floatingSpace = Math.min(insertPoint.z - maxZBelow - 1, lastPushDown);

			if (floatingSpace == 0) {
				minAllowedZ = insertPoint.z;
				break;
			}
			lastPushDown = floatingSpace;

			for (Point b : p.boundaries) {
				b.z -= floatingSpace;
			}
			insertPoint = p.location();

			if (insertPoint.z > minAllowedZ) {
				minAllowedZ = insertPoint.z;
			}
			for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + p.xSize; xi++) {
				for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + p.ySize; yi++) {
					top[xi][yi] -= floatingSpace;
				}
			}
		}
	}

	private void occupy(Present present, Point oneBasedInsertionPoint) {
		for (int p = oneBasedInsertionPoint.x - 1; p < oneBasedInsertionPoint.x - 1 + present.xSize; p++) {
			for (int q = oneBasedInsertionPoint.y - 1; q < oneBasedInsertionPoint.y - 1 + present.ySize; q++) {
				top[p][q] = present.maxZ();
			}
		}
	}

	public List<Present> sortNatural(List<Present> layer) {
		Comparator<Present> comparator = naturalComparator();
		List<Present> sortedCopy = Ordering.from(comparator).sortedCopy(layer);
		return sortedCopy;
	}

	private int maxZBelow(Present present) {
		Point insertPoint = present.location();
		return maxZBelow(present, insertPoint);
	}

	private int maxZBelow(Present present, Point insertPoint) {
		int maxZBelow = 0;
		for (int xi = insertPoint.x - 1; xi < insertPoint.x - 1 + present.xSize; xi++) {
			for (int yi = insertPoint.y - 1; yi < insertPoint.y - 1 + present.ySize; yi++) {
				int zi = prevTop[xi][yi];
				if (zi > maxZBelow) {
					maxZBelow = zi;
				}
			}
		}
		return maxZBelow;
	}

	private Comparator<Present> naturalComparator() {
		return new Comparator<Present>() {

			@Override
			public int compare(Present o1, Present o2) {
				return Ints.compare(o1.order, o2.order);
			}
		};
	}

	private int computeMaxZFromTop() {
		int maxZ = -1;
		for (int xi = 0; xi < 1000; xi++) {
			for (int yi = 0; yi < 1000; yi++) {
				if (top[xi][yi] > maxZ) {
					maxZ = top[xi][yi];
				}
			}
		}

		return maxZ;
	}
}
