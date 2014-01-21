package pipi;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Ints;

public class PresentBatch {
	private Deque<QueuedPresent> presentsStack = Queues.newArrayDeque();
	private PriorityQueue<QueuedPresent> heightsHeap = new PriorityQueue<>(100, new Comparator<QueuedPresent>() {

		@Override
		public int compare(QueuedPresent o1, QueuedPresent o2) {
			int compare = Ints.compare(o2.orientedDimension3d.height, o1.orientedDimension3d.height);
			if (compare != 0) {
				return compare;
			}
			return Ints.compare(o1.orientation, o2.orientation);
		}
	});
	private int area;
	private int volume;
	private final int maxArea;

	public PresentBatch() {
		this(1000 * 1000);
	}

	public PresentBatch(int initialArea) {
		this.maxArea = initialArea;
	}

	public void popPresent() {
		QueuedPresent last = this.presentsStack.getLast();
		this.presentsStack.removeLast();
		this.area -= last.orientedDimension3d.base.area();
		this.volume -= last.orientedDimension3d.volume();
		boolean remove = this.heightsHeap.remove(last);
		assert remove;
	}

	public int size() {
		return this.presentsStack.size();
	}

	public boolean pushPresent(Dimension3d dimension, int orientation) {
		QueuedPresent orientedDimension3d = createQueuedPresent(dimension, orientation);
		int newArea = this.area + orientedDimension3d.orientedDimension3d.base.area();
		if (newArea > this.maxArea) {
			return false;
		}
		this.area = newArea;
		this.volume += orientedDimension3d.orientedDimension3d.volume();
		this.presentsStack.add(orientedDimension3d);
		this.heightsHeap.offer(orientedDimension3d);
		return true;
	}

	private QueuedPresent createQueuedPresent(Dimension3d dimension, int orientation) {
		return new QueuedPresent(dimension, dimension.getRotation(orientation), orientation);
	}

	public int getArea() {
		return this.area;
	}

	public int getVolume() {
		return this.volume;
	}

	public int maxArea() {
		return this.maxArea;
	}

	public int maxVolume() {
		return this.heightsHeap.peek().orientedDimension3d.height * this.maxArea;
	}

	@Override
	public String toString() {
		return String.format("Area: %d Max: %d %%: %2.2f | Volume: %d Max: %d %%: %2.2f", this.getArea(), this.maxArea(),
				(double) this.getArea() / this.maxArea(), this.getVolume(), this.maxVolume(), (double) this.getVolume()
						/ this.maxVolume());
	}

	public boolean canChangeMaximumZ() {
		return this.heightsHeap.peek().orientation != 2;
	}

	public boolean rotateMaximumZ() {
		QueuedPresent queuedPresent = this.heightsHeap.peek();
		QueuedPresent createQueuedPresent = createQueuedPresent(queuedPresent.dimension, queuedPresent.orientation + 1);
		int newArea = this.area - queuedPresent.orientedDimension3d.base.area()
				+ createQueuedPresent.orientedDimension3d.base.area();
		if (newArea > this.maxArea()) {
			return false;
		}
		this.area = newArea;
		boolean remove = this.heightsHeap.remove(queuedPresent);
		assert remove;
		QueuedPresent rotatedQueuedPresent = rotate(queuedPresent);
		this.heightsHeap.offer(rotatedQueuedPresent);
		return true;
	}

	private QueuedPresent rotate(QueuedPresent queuedPresent) {
		
		OrientedDimension3d orientedDimension3d = queuedPresent.orientedDimension3d;
		Dimension2d base = orientedDimension3d.base;
		Dimension3d dimension3d = Dimension3d.create(base.small, base.large, orientedDimension3d.height);
		
		queuedPresent.orientation++;
		OrientedDimension3d rotation = dimension3d.getRotation(queuedPresent.orientation);
		queuedPresent.orientedDimension3d = rotation;
		return queuedPresent;
	}

	public double usage() {
		return (double)this.getVolume() / this.maxVolume();
	}

	public List<OrientedDimension3d> getPresents(){
		List<OrientedDimension3d> orientedDimension3ds = Lists.newArrayList();
		for (QueuedPresent queuedPresent : this.presentsStack) {
			orientedDimension3ds.add(queuedPresent.orientedDimension3d);
		}
		return orientedDimension3ds;
	}
}
