package pipi.packer;

import java.util.List;
import java.util.Set;

import pipi.interval.PutRectangle;

public class PackResult {

	private List<PutRectangle> putRectangles;
	private Set<Integer> putIndexes;
	private Set<Integer> notIndexes;

	public PackResult(List<PutRectangle> result, Set<Integer> putIndexes, Set<Integer> notIndexes) {
		this.putRectangles = result;
		this.putIndexes = putIndexes;
		this.notIndexes = notIndexes;
	}

	public List<PutRectangle> getPutRectangles() {
		return this.putRectangles;
	}

	public Set<Integer> getPutIndexes() {
		return putIndexes;
	}

	public Set<Integer> getNotIndexes() {
		return notIndexes;
	}

}
