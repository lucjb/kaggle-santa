package pipi.packer;

import java.util.Collection;
import java.util.List;

import pipi.Dimension2d;
import pipi.interval.Rectangle;

public class CompositePacker implements Packer {

	private final Packer[] packers;

	public CompositePacker(Packer... packers) {
		this.packers = packers;
	}
	
	@Override
	public List<Rectangle> packPesents(Collection<Dimension2d> dimensions) {
		Packer[] a = this.packers;
		List<Rectangle> bestPackedPesents = null;
		for (Packer packer : a) {
			List<Rectangle> packedPesents = packer.packPesents(dimensions);
			if(bestPackedPesents == null || packedPesents.size() > bestPackedPesents.size()){
				bestPackedPesents = packedPesents;
			}
		}
		return bestPackedPesents;
	}

}
