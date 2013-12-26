package pipi.interval;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.collect.Maps;

public class SleighColumn {
	
	private Line ranges;
	private Line lines;

	public SleighColumn(IntRange verticalRange) {
		this.ranges = new Line(verticalRange);
		this.lines = new Line(verticalRange);
	}
	
	public void addLine(IntRange verticalIntRange) {
		this.lines.addRange(verticalIntRange);
	}

	public IntRange getLine(int y) {
		return this.lines.getRange(y);
	}

	public void destroy(IntRange verticalRange) {
		this.lines.removeRange(verticalRange);
	}

	public Collection<IntRange> getLines() {
		return this.lines.getRanges();
	}
	
	@Override
	public String toString() {
		return this.lines.toString() + "->" + this.ranges.toString();
	}

	public boolean isEmpty() {
		return this.lines.isEmpty();
	}
}
