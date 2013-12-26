package pipi.interval;

import java.util.Collection;
import java.util.List;

public class SleighColumn {
	
	private IntervalSet faces;
	private IntervalSet lines;

	public SleighColumn(Interval verticalRange) {
		this.faces = new IntervalSet(verticalRange);
		this.lines = new IntervalSet(verticalRange);
	}
	
	public void addLine(Interval verticalIntRange) {
		this.lines.addRange(verticalIntRange);
	}

	public Interval getLine(int y) {
		return this.lines.getRange(y);
	}

	public void destroy(Interval verticalRange) {
		this.lines.removeRange(verticalRange);
		this.faces.removeRange(verticalRange);
	}

	public Collection<Interval> getLinesRanges() {
		return this.lines.getRanges();
	}
	
	@Override
	public String toString() {
		return this.lines.toString() + "->" + this.faces.toString();
	}

	public boolean isEmpty() {
		return this.lines.isEmpty();
	}
	
	public IntervalSet getRanges() {
		return this.faces;
	}
	
	public IntervalSet getLines() {
		return this.lines;
	}
	
	public void addInterval(Interval interval){
		//FIXME optimize
		this.getRanges().addRange(interval);
		List<Interval> emptyRanges = this.lines.getEmptyRanges(interval);
		this.getRanges().removeAllRanges(emptyRanges);
	}

	public void removeLines(Collection<Interval> intervals){
		for (Interval interval : intervals) {
			this.destroy(interval);
		}
	}
	
}
