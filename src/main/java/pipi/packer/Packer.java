package pipi.packer;

import java.util.Collection;
import java.util.List;

import pipi.OrientedDimension3d;
import pipi.interval.Rectangle;

public interface Packer {

	public abstract PackResult packPesents(List<OrientedDimension3d> dimensions);

	public abstract void freeAll(Collection<Rectangle> prefill);

	public abstract boolean isEmpty();

	public abstract void parasite(Packer packer);

}