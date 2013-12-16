package first;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class FastXYCompactSleigh {
	
	static class Point2D implements Comparable<Point2D>{
		int x;
		int y;
		
		Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int compareTo(Point2D o) {
			int yComp = Integer.compare(y, o.y);
			if (yComp != 0)
				return yComp;
			return Integer.compare(x, o.x);
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}
	}
	
	static class Surface2D {
		private BitSet surface = new BitSet(1000 * 1000);
		
		public boolean occupied(int x, int y) {
			return surface.get(x * 1000 + y);
		}
		
		public void occupy(int x, int y) {
			surface.set(x * 1000 + y);
		}
		
		public void clear() {
			surface.clear();
		}
	}

	Surface2D floor = new Surface2D();
	int currentZ = 1;
	int nextZ = 0;
	int layerCount = 1;
	SortedSet<Point2D> insertionPoints = new TreeSet<Point2D>();
	
	public FastXYCompactSleigh() {
		insertionPoints.add(new Point2D(0, 0));
	}

	public void addPresents(List<Present> presents) {
//		for (Present present : presents) {
//			if (present.order % 10000 == 0)
//				System.out.println(present.order);
//			if (!add(present)) {
//				startNewLayer();
//				insert(present, new Point2D(0, 0));
//			}
//		}
		
		addReordering(presents);

		System.out.println("Total layers: " + layerCount);
		
		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = nextZ - present.boundaries.get(i).z;
			}
		}
	}
	
	private void addReordering(List<Present> presents) {
		List<Present> layerOrder = Lists.newArrayList();
		
		for (Present present : presents) {
			if (!add(present)) {
				undoLayerWithPresents(layerOrder);
				List<Present> sortedLayer = sortByArea(layerOrder);
				if (insertAll(sortedLayer)) {
					layerOrder = sortedLayer;
				} else {
					undoLayerWithPresents(sortedLayer);
					if (!insertAll(layerOrder)) {
						throw new RuntimeException("foo!");
					}
				}
				if (!add(present)) {
					layerOrder.clear();
					startNewLayer();
					add(present);
				}
			}
			layerOrder.add(present);
			
			if (present.order % 10000 == 0) {
				System.out.println(present.order);
			}
		}
	}
	
	private boolean insertAll(List<Present> sortedCopy) {
		for (Present p : sortedCopy) {
			if (!add(p)) {
				return false;
			}
		}
		return true;
	}
	
	private void undoLayerWithPresents(List<Present> layer) {
		for (Present present : layer) {
			present.boundaries.clear();
		}
		initializeLayer();
	}
	
	private List<Present> sortByArea(List<Present> layer) {
		List<Present> sortedCopy = new ArrayList<>(layer);
		Collections.sort(sortedCopy, new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				return -Ints.compare(o1.xSize * o1.ySize, o2.xSize * o2.ySize);
			}
		});
		return sortedCopy;
	}
	
	private boolean add(Present present) {
		present.rotateMinMedMax();
		Point2D insertPoint = findBLInsertionPoint(present);
		if (insertPoint == null) {
			present.rotate();
			insertPoint = findBLInsertionPoint(present);
		}
		if (insertPoint != null) {
			insert(present, insertPoint);
			return true;
		}	
		return false;
	}
	
	private Point2D findBLInsertionPoint(Present present) {
		for (Point2D point : insertionPoints) {
			//try to move down and try to move left (at most only one of those is going to move)
			int newY = point.y;
			while(nextHorizontalLineIsFeasible(point.x, newY, present.xSize)) {
				newY--;
			}
			int newX = point.x;
			while(previousVerticalLineIsFeasible(newX, newY, present.ySize)) {
				newX--;
			}			
			
			Point2D newPoint = new Point2D(newX, newY); 
			if (fits(newPoint, present.xSize, present.ySize)) {
				return newPoint;
			}
		}
		return null;
	}
	
	private boolean previousVerticalLineIsFeasible(int x, int y, int height) {
		int prevX = x - 1;
		if (prevX < 0 || y + height >= 1000)
			return false;
		
		for (int q = y + height - 1; q >= y; q--) {
			if (floor.occupied(prevX, q)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean nextHorizontalLineIsFeasible(int x, int y, int width) {
		int nextY = y - 1;
		if (nextY < 0 || x + width >= 1000)
			return false;
		
		for (int p = x + width - 1; p >= x; p--) {
			if (floor.occupied(p, nextY)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean fits(Point2D point, int xSize, int ySize) {
		if (point.x + xSize >= 1000 || point.y + ySize >= 1000)
			return false;
		
		for (int p = point.x + xSize - 1; p >= point.x; p--) {
			for (int q = point.y + ySize - 1; q >= point.y ; q--) {
				if (floor.occupied(p, q)) {
					return false;
				}
			}
		}
		return true;
	}

	private void startNewLayer() {
		currentZ = nextZ;
		layerCount++;
		initializeLayer();
	}

	private void initializeLayer() {
		nextZ = 0;
		floor.clear();
		insertionPoints.clear();
		insertionPoints.add(new Point2D(0, 0));
	}

	private void insert(Present present, Point2D insertionPoint) {
		this.occupy(insertionPoint.x, insertionPoint.y, present.xSize, present.ySize);
		
		//Might not be present if we moved down or left
		insertionPoints.remove(insertionPoint);
		if (insertionPoint.x + present.xSize < 1000 && !floor.occupied(insertionPoint.x + present.xSize, insertionPoint.y)) {
			insertionPoints.add(new Point2D(insertionPoint.x + present.xSize, insertionPoint.y));
		}
		if (insertionPoint.y + present.ySize < 1000 && !floor.occupied(insertionPoint.x, insertionPoint.y + present.ySize)) {
			insertionPoints.add(new Point2D(insertionPoint.x, insertionPoint.y + present.ySize));
		}
		
		Point oneBasedInsertionPoint = new Point(insertionPoint.x + 1, insertionPoint.y + 1, currentZ);
		setBoundaries(present, oneBasedInsertionPoint);

		int presentMaxZ = oneBasedInsertionPoint.z + present.zSize - 1;
		if (presentMaxZ >= nextZ) {
			nextZ = presentMaxZ + 1;
		}
		
//		System.out.println(present.order + " [" + present.xSize + ","+ present.ySize +","+ present.zSize +"] "+ oneBasedInsertionPoint);
	}

	private void setBoundaries(Present present, Point insertPoint) {
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y + present.ySize - 1, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y + present.ySize - 1, insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y + present.ySize - 1, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1, insertPoint.y + present.ySize - 1, insertPoint.z + present.zSize - 1));
	}
	

	private void occupy(int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				floor.occupy(p, q);
			}
		}
	}

}
