package pipi.interval;

import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.collect.Maps;

public class SleighColumn {
	private TreeMap<Integer, Line> lines = Maps.newTreeMap();

	public void addLine(Line sleighLine) {
		this.lines.put(sleighLine.getRange().getFrom(), sleighLine);
	}

	public Line getLine(int y) {
		Entry<Integer, Line> floorEntry = this.lines.floorEntry(y);
		Line line = floorEntry.getValue();
		if (line.getRange().getTo() > y) {
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
			max = this.lines.lastEntry().getValue().getRange().getTo();
		}else{
			max = ceilingEntry.getValue().getRange().getFrom();
		}
		
		this.lines.subMap(floorEntry.getKey(), max);
		/// FIXME!!! iterate
	}

}
