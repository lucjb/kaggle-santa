package pipi.packer;

import java.util.Collection;
import java.util.List;

import pipi.Dimension2d;
import pipi.interval.Rectangle;

public interface Packer {

	public abstract List<Rectangle> packPesents(Collection<Dimension2d> dimensions);

	public abstract void preFill(Collection<Rectangle> prefill);

	public abstract void freeAll(Collection<Rectangle> prefill);

	public abstract boolean isEmpty();

}