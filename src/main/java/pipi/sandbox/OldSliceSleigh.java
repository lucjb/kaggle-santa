package pipi.sandbox;

import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import pipi.Box3d;
import pipi.Slice;
import first.Point;

public class OldSliceSleigh {
	private Map<Integer, BitsetSlice> slices = Maps.newHashMap();
	private PriorityQueue<Integer> levels = Queues.newPriorityQueue();

	private int currentZ;

	public Slice getSlice(int level) {
		BitsetSlice sleighSlice = this.slices.get(level);
		if (sleighSlice == null) {
			sleighSlice = BitsetSlice.filled(1000);
			this.slices.put(level, sleighSlice);
			this.levels.offer(level);
		}
		return sleighSlice;
	}

	public OldSliceSleigh() {
		this.slices.put(0, BitsetSlice.filled(1000));
		this.currentZ = 0;
	}

	public Point putPesent(Box3d box3d) {
		for (;;) {
			BitsetSlice sleighSlice = this.slices.get(this.currentZ);
			for (int x = 0; x <= 1000 - box3d.dx; x++) {
				for (int y = 0; y <= 1000 - box3d.dy; y++) {
					boolean canContain = sleighSlice.isFree(x, y, box3d.dx, box3d.dy);
					if (canContain) {
						sleighSlice.free(x, y, box3d.dx, box3d.dy);
						int newZ = this.currentZ + box3d.dz;
						Slice zSleighSlice = this.getSlice(newZ);
						zSleighSlice.fill(x, y, box3d.dx, box3d.dy);
						return new Point(x, y, this.currentZ);
					}
				}
			}
			this.currentZ = this.levels.remove();
			this.slices.get(this.currentZ).merge(sleighSlice);
		}
	}

	public int getLastZ() {
		Integer lastZ;
		do {
			lastZ = levels.poll();
		} while (!levels.isEmpty());
		return lastZ;
	}
	
	public int getCurrentZ() {
		return currentZ;
	}

}
