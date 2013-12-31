package pipi.interval;

import java.util.Collection;
import java.util.List;

public class SleighColumn {
	
	private IntervalSet sides;
	private IntervalSet lines;

	public SleighColumn(Interval verticalRange) {
		this.sides = new IntervalSet(verticalRange);
		this.lines = new IntervalSet(verticalRange);
	}
	
	public void addLine(Interval verticalIntRange) {
		this.lines.addRange(verticalIntRange);
	}

	public Interval getLine(int y) {
		return this.lines.getRange(y);
	}

	public void destroy(Interval verticalRange) {
		//FIXME optimize
		this.lines.removeRange(verticalRange);
		this.sides.removeRange(verticalRange);
		List<Interval> ranges = this.lines.getRanges();
		for (Interval interval : ranges) {
			List<Interval> ranges2 = this.sides.getIntervals(interval).getRanges();
			if(ranges2.isEmpty()){
				this.lines.removeRange(interval);
			}
		}
	}

	public Collection<Interval> getLinesRanges() {
		return this.lines.getRanges();
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
	
	public void addInterval(Interval interval){
		//FIXME optimize
		this.getSides().addRange(interval);
		IntervalSet emptyRanges = this.lines.complement().getIntervals(interval);
		this.getSides().removeAllRanges(emptyRanges);
	}

	public void removeLines(Collection<Interval> intervals){
		for (Interval interval : intervals) {
			this.destroy(interval);
		}
	}
	
}
