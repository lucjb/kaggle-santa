package pipi.main;

import java.util.List;

import pipi.OrientedDimension3d;
import pipi.PresentBatch;
import pipi.SuperPresent;
import pipi.interval.PutRectangle;

import com.google.common.collect.Multimap;

public class PackedBatch {

	private List<PutRectangle> bestPackedPresents;
	private Multimap<OrientedDimension3d, SuperPresent> bestPresents;
	private PresentBatch bestPresentBatch;

	public PackedBatch(List<PutRectangle> bestPackedPresents, Multimap<OrientedDimension3d, SuperPresent> bestPresents,
			PresentBatch bestPresentBatch) {
		this.bestPackedPresents = bestPackedPresents;
		this.bestPresents = bestPresents;
		this.bestPresentBatch = bestPresentBatch;
	}

	public List<PutRectangle> getBestPackedPresents() {
		return this.bestPackedPresents;
	}

	public Multimap<OrientedDimension3d, SuperPresent> getBestPresents() {
		return this.bestPresents;
	}

	public PresentBatch getBestPresentBatch() {
		return this.bestPresentBatch;
	}

	
	
}
