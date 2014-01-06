package pipi.interval;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import pipi.Box2d;
import pipi.Box3d;
import pipi.Dimension2d;
import pipi.Dimension3d;
import pipi.OutputPresent;
import pipi.SuperPresent;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import first.Point;

public class IntervalSleigh {

	private int currentZ;
	private int lastZ = 0;
	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalSleigh() {
		this.currentZ = 0;
	}

	public void emitPresents(List<Rectangle> putPesent, Multimap<Dimension2d, SuperPresent> presentsWithDimension) {
		int nextZ = this.currentZ;
		for (Rectangle rectangle : putPesent) {
			Dimension2d dimension = rectangle.getBox2d().dimension();
			SuperPresent present = presentsWithDimension.get(dimension).iterator().next();
			presentsWithDimension.remove(dimension, present);
			Rectangle min = rectangle;
			Box3d box3d = new Box3d(rectangle.box2d.dx, rectangle.box2d.dy, present.getDimension().large);
			this.outputPresents.add(new OutputPresent(present.getOrder(), new Point(min.point2d.x, min.point2d.y,
					this.currentZ), box3d));
			nextZ = Math.max(nextZ, this.currentZ + box3d.dz);
		}
		this.currentZ = nextZ;
	}

	public int getLastZ() {
		return this.lastZ;
	}

	public int getCurrentZ() {
		return this.currentZ;
	}

	public List<OutputPresent> getOutputPresents() {
		return this.outputPresents;
	}

}
