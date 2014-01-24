package pipi.sleigh;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import pipi.interval.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;

public class FloorStructure {
	private MinMaxPriorityQueue<RectangleFloor> floorsHeap = MinMaxPriorityQueue.create();
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

	public List<RectangleFloor> getRectangleFloors(){
		RectangleFloor[] array = new RectangleFloor[this.floorsHeap.size()]; 
		array = this.floorsHeap.toArray(array);
		Arrays.sort(array);
		return Lists.newArrayList(array);
	}
	
	public int floorCount(){
		return this.floorsHeap.size();
	}
	
	public int maxZ(){
		return this.floorsHeap.peekLast().getHeight();
	}
}
