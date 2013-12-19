package pipi;

import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import pipi.bitmatrix.SleighSlice;
import first.Point;

public class SliceSuperSleigh {
	private Map<Integer, SleighSlice> slices = Maps.newHashMap();
	private PriorityQueue<Integer> levels = Queues.newPriorityQueue();

	private int currentZ;

	public SleighSlice getSlice(int level) {
		SleighSlice sleighSlice = this.slices.get(level);
		if (sleighSlice == null) {
			sleighSlice = new SleighSlice();
			this.slices.put(level, sleighSlice);
			this.levels.offer(level);
		}
		return sleighSlice;
	}

	public SliceSuperSleigh() {
		this.slices.put(0, SleighSlice.filled());
		this.currentZ = 0;
	}

	public Point putPesent(Box box) {
		for (;;) {
			SleighSlice sleighSlice = this.slices.get(this.currentZ);
			for (int x = 0; x <= 1000 - box.dx; x++) {
				for (int y = 0; x <= 1000 - box.dy; y++) {
					if (sleighSlice.canContain(x, y, box.dx, box.dy)) {
						sleighSlice.clear(x, y, box.dx, box.dy);
						int newZ = this.currentZ + box.dz;
						SleighSlice zSleighSlice = this.getSlice(newZ);
						zSleighSlice.set(x, y, box.dx, box.dy);
						return new Point(x, y, this.currentZ);
					}
				}
			}
			this.currentZ = this.levels.remove();
			return null;
		}

	}

}
