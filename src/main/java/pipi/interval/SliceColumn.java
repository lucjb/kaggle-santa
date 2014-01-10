package pipi.interval;

import java.util.Collection;
import java.util.List;

public class SliceColumn {
	
	private IntervalSet sides;
	private IntervalSet lines;

	public SliceColumn(Interval verticalRange) {
		this.sides = IntervalSlice.buildIntervalSet(verticalRange.getTo());
		this.lines = IntervalSlice.buildIntervalSet(verticalRange.getTo());
	}
	
	public void addLine(Interval verticalIntRange) {
		this.lines.addInterval(verticalIntRange);
	}


	public void destroy(Interval verticalRange) {
		//FIXME optimize
		this.lines.removeInterval(verticalRange);
		this.sides.removeInterval(verticalRange);
		removeEmptyLines();
	}

	public void removeEmptyLines() {
		List<Interval> ranges = this.lines.getIntervals();
		for (Interval interval : ranges) {
			List<Interval> ranges2 = this.sides.getContainedIntervals(interval).getIntervals();
			if(ranges2.isEmpty()){
				this.lines.removeInterval(interval);
			}
		}
	}

	public Collection<Interval> getLinesRanges() {
		return this.lines.getIntervals();
	}
	
	@Override
	public String toString() {
		return this.lines.toString() + "->" + this.sides.toString();
	}

	public boolean isEmpty() {
		return this.lines.isEmpty();
	}
	
	public IntervalSet getSides() {
		return this.sides;
	}
	
	public IntervalSet getLines() {
		return this.lines;
	}
	
	public void addSide(Interval interval){
		//FIXME optimize
		this.getSides().addInterval(interval);
		removeEmptyLines(interval);
	}

	private void removeEmptyLines(Interval interval) {
		IntervalSet emptyRanges = this.lines.getComplement().getContainedIntervals(interval);
		this.sides.removeAllRanges(emptyRanges);
	}

	public void removeLines(Collection<Interval> intervals){
		for (Interval interval : intervals) {
			this.destroy(interval);
		}
	}
	
}
