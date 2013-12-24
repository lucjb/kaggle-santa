package pipi.interval;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.collect.Maps;

public class SleighColumn {
	private TreeMap<Integer, Line> lines = Maps.newTreeMap();

	public void addLine(Line sleighLine) {
		this.lines.put(sleighLine.getSpan().getFrom(), sleighLine);
	}

	public Line getLine(int y) {
		Entry<Integer, Line> floorEntry = this.lines.floorEntry(y);
		Line line = floorEntry.getValue();
		if (line.getSpan().getTo() > y) {
			return line;
		}
		return null;
	}

	public void destroy(IntRange verticalRange) {
		Entry<Integer, Line> floorEntry = this.lines.floorEntry(verticalRange.getFrom());
		if(floorEntry == null){
			floorEntry = this.lines.firstEntry();
		}
		int max;
		Entry<Integer, Line> ceilingEntry = this.lines.ceilingEntry(verticalRange.getTo());
		if(ceilingEntry == null){
			max = this.lines.lastEntry().getValue().getSpan().getTo();
		}else{
			max = ceilingEntry.getValue().getSpan().getFrom();
		}
		
		this.lines.subMap(floorEntry.getKey(), max);
		/// FIXME!!! iterate
	}

	public Collection<Line> getLines() {
		return this.lines.values();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.lines.toString();
	}
}
