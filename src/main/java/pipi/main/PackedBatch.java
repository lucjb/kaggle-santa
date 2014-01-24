package pipi.main;

import pipi.OrientedDimension3d;
import pipi.PresentBatch;
import pipi.SuperPresent;
import pipi.packer.PackResult;

import com.google.common.collect.Multimap;

public class PackedBatch {

	private PackResult bestPackedPresents;
	private Multimap<OrientedDimension3d, SuperPresent> bestPresents;
	private PresentBatch bestPresentBatch;

	public PackedBatch(PackResult bestPackedPresents2, Multimap<OrientedDimension3d, SuperPresent> bestPresents,
			PresentBatch bestPresentBatch) {
		this.bestPackedPresents = bestPackedPresents2;
		this.bestPresents = bestPresents;
		this.bestPresentBatch = bestPresentBatch;
	}

	public PackResult getBestPackedPresents() {
		return this.bestPackedPresents;
	}

	public Multimap<OrientedDimension3d, SuperPresent> getBestPresents() {
		return this.bestPresents;
	}

	public PresentBatch getBestPresentBatch() {
		return this.bestPresentBatch;
	}

	
	
}
