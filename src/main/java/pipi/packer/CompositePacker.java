package pipi.packer;

import java.util.Collection;
import java.util.List;

import pipi.OrientedDimension3d;
import pipi.interval.PutRectangle;
import pipi.interval.Rectangle;

public class CompositePacker implements Packer {

	private final Packer[] packers;

	public CompositePacker(Packer... packers) {
		this.packers = packers;
	}
	
	@Override
	public List<PutRectangle> packPesents(Collection<OrientedDimension3d> dimensions) {
		List<PutRectangle> bestPackedPesents = null;
		for (Packer packer : this.packers) {
			List<PutRectangle> packedPesents = packer.packPesents(dimensions);
			if(bestPackedPesents == null || packedPesents.size() > bestPackedPesents.size()){
				bestPackedPesents = packedPesents;
			}
		}
		return bestPackedPesents;
	}

	@Override
	public void freeAll(Collection<Rectangle> prefill) {
		for (Packer packer : this.packers) {
			packer.freeAll(prefill);
		}
	}

	@Override
	public boolean isEmpty() {
		throw new RuntimeException();
	}

}
