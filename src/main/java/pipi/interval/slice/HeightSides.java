package pipi.interval.slice;

import pipi.interval.Interval;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

public class HeightSides {
	public HeightSet[] sides;
	private int extension;
	private int columns;

	private HeightSides(int columns, int extension) {
		this.columns = columns;
		this.extension = extension;
		this.sides = new HeightSet[this.columns + 1];
	}

	public static HeightSides empty(int columns, int extension) {
		HeightSides cachedSides = new HeightSides(columns, extension);
		return cachedSides;
	}

	public void addSide(Interval verticalRange, int insertionPoint, int height) {
		HeightSet sliceColumn = this.sides[insertionPoint];
		if (sliceColumn == null) {
			sliceColumn = new HeightSet(this.extension);
			this.sides[insertionPoint] = sliceColumn;
		}
		sliceColumn.addInterval(verticalRange, height);
	}

	public void removeSide(Interval verticalRange, int insertionPoint) {
		HeightSet sliceColumn = this.sides[insertionPoint];
		if (sliceColumn == null) {
			return;
		}
		sliceColumn.removeInterval(verticalRange);
		if (sliceColumn.isEmpty()) {
			this.sides[insertionPoint] = null;
		}
	}

	public Multiset<Integer> getSideInfo(int point, Interval verticalRange) {
		HeightSet heightSet = this.sides[point];
		if (heightSet == null) {
			return ImmutableMultiset.of();
		}
		return heightSet.getSideInfo(verticalRange);
	}

	public HeightSides copy() {
		HeightSides heightSides = new HeightSides(this.columns, this.extension);
		for (int i = 0; i < this.sides.length; i++) {
			HeightSet heightSet = this.sides[i];
			if (heightSet != null) {
				heightSides.sides[i] = heightSet.copy();
			}
		}
		return heightSides;
	}

}