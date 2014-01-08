package pipi;

import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;

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
	private final int maxArea = 1000 * 1000;

	public PresentBatch() {

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
		switch (orientation) {
		case 0:
			return new QueuedPresent(dimension, dimension.smallOriented(), orientation);
		case 1:
			return new QueuedPresent(dimension, dimension.mediumOriented(), orientation);
		case 2:
			return new QueuedPresent(dimension, dimension.largeOriented(), orientation);
		}
		throw new RuntimeException();
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
		switch (queuedPresent.orientation) {
		case 0:
			queuedPresent.orientation++;
			queuedPresent.orientedDimension3d = queuedPresent.dimension.mediumOriented();
			return queuedPresent;
		case 1:
			queuedPresent.orientation++;
			queuedPresent.orientedDimension3d = queuedPresent.dimension.largeOriented();
			return queuedPresent;
		}
		throw new RuntimeException();
	}

	public double usage() {
		return (double)this.getVolume() / this.maxVolume();
	}

}
