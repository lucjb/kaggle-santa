package pipi.sleigh;

import java.util.Map;
import java.util.PriorityQueue;

import pipi.interval.Rectangle;

import com.google.common.collect.Maps;

public class FloorStructure {
	private PriorityQueue<RectangleFloor> floorsHeap = new PriorityQueue<>();
	private Map<Integer, RectangleFloor> floorsMap = Maps.newHashMap();
	private int currentZ = 0;
	
	public FloorStructure() {
	}

	public void putPresent(int height, Rectangle rectangle) {
		RectangleFloor floor = getFloor(height);
		floor.getRectangles().add(rectangle);
	}

	private RectangleFloor getFloor(int height) {
		RectangleFloor floor = this.floorsMap.get(height);
		if (floor == null) {
			floor = new RectangleFloor(height);
			this.floorsMap.put(height, floor);
			this.floorsHeap.offer(floor);
		}
		return floor;
	}

	public RectangleFloor bottomFloor() {
		return this.floorsHeap.peek();
	}

	public RectangleFloor popFloor() {
		RectangleFloor remove = this.floorsHeap.poll();
		if(remove == null){
			return null;
		}
		this.floorsMap.remove(remove.getHeight());
		this.currentZ = remove.getHeight();
		return remove;
	}

	public int getCurrentZ() {
		return this.currentZ;
	}

}
