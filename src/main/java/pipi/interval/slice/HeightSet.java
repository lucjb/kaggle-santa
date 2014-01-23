package pipi.interval.slice;

import java.util.Arrays;

import pipi.interval.Interval;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.primitives.Ints;

public class HeightSet {

	private final int[] heights;
	private int fill = 0;

	public HeightSet(int extension) {
		this.heights = new int[extension];
	}

	public void addInterval(Interval verticalRange, int height) {
		assert Collections2.filter(Ints.asList(this.heights).subList(verticalRange.getFrom(), verticalRange.getTo()),
				new Predicate<Integer>() {
					public boolean apply(Integer integer) {
						return integer != 0;
					}
				}).isEmpty();

		Arrays.fill(this.heights, verticalRange.getFrom(), verticalRange.getTo(), height);
		this.fill += verticalRange.length();
	}

	public void removeInterval(Interval verticalRange) {
		assert Ints.asList(this.heights).subList(verticalRange.getFrom(), verticalRange.getTo()).indexOf(0) == -1;

		Arrays.fill(this.heights, verticalRange.getFrom(), verticalRange.getTo(), 0);
		this.fill -= verticalRange.length();
	}

	public boolean isEmpty() {
		return this.fill == 0;
	}

	public Multiset<Integer> getSideInfo(Interval verticalRange) {
		Multiset<Integer> multiset = HashMultiset.create();
		for (int i = verticalRange.getFrom(); i < verticalRange.getTo(); i++) {
			multiset.add(this.heights[i]);
		}
		return multiset;
	}

	public HeightSet copy() {
		HeightSet heightSet = new HeightSet(this.heights.length);
		heightSet.fill = this.fill;
		System.arraycopy(this.heights, 0, heightSet.heights, 0, this.heights.length);
		return heightSet;
	}

}
