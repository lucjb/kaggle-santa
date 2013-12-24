package pipi.interval;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class Column {
	private RangeSet<Integer> segments = TreeRangeSet.create();
	private RangeSet<Integer> lines = TreeRangeSet.create();
	
}
