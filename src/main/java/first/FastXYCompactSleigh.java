package first;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

public class FastXYCompactSleigh {

	static class Point2D implements Comparable<Point2D> {
		int x;
		int y;

		Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Point2D o) {
			int sumComp = Integer.compare(x + y, o.x + o.y);
			if (sumComp != 0)
				return sumComp;
			return Integer.compare(x, o.x);
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}
	}

	static class Surface2D implements Cloneable {
		int zLevel = 0;
		int maxPresentHeight = 0;
		private BitSet surface = new BitSet(1000 * 1000);
		SortedSet<Point2D> insertionPoints;
		Comparator<Point2D> ipComparator;

		public Surface2D(Comparator<Point2D> insertionPointComparator) {
			ipComparator = insertionPointComparator;
			insertionPoints = new TreeSet<Point2D>(insertionPointComparator);
			insertionPoints.add(new Point2D(0, 0));
		}
		
		public boolean occupied(int x, int y) {
			return surface.get(x * 1000 + y);
		}

		public void occupy(int x, int y) {
			surface.set(x * 1000 + y);
		}

		public void free(int x, int y) {
			surface.clear(x * 1000 + y);
		}
		
		public void clear() {
			surface.clear();
			maxPresentHeight = 0;
			insertionPoints.clear();
			insertionPoints.add(new Point2D(0, 0));
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			Surface2D clone = new Surface2D(ipComparator);
			clone.surface = (BitSet)surface.clone();
			clone.insertionPoints = (SortedSet<Point2D>) ((TreeSet<Point2D>)insertionPoints).clone();
			clone.zLevel = zLevel;
			clone.maxPresentHeight = maxPresentHeight;
			return clone;
		}
	}

	int layerCount = 1;

	public void addPresents(List<Present> presents) {
		int zeroBasedMaxOccupiedZ = addReordering(presents);

		System.out.println("Total layers: " + layerCount);

		for (Present present : presents) {
			for (int i = 0; i < 8; i++) {
				present.boundaries.get(i).z = zeroBasedMaxOccupiedZ + 1 - present.boundaries.get(i).z;
			}
		}
	}
	
	private List<Present> clonePresents(List<Present> presents) {
		List<Present> presentsClones = new ArrayList<Present>(1000000);
		for (Present present : presents) {
			try {
				presentsClones.add((Present)present.clone());
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
		return presentsClones;
	}

	private int addReordering(List<Present> presents) {
		int i = 0;
		Surface2D floor = new Surface2D(new Comparator<FastXYCompactSleigh.Point2D>() {
			@Override
			public int compare(Point2D o1, Point2D o2) {
				int sumComp = Integer.compare(o1.x + o1.y, o2.x + o2.y);
				if (sumComp != 0)
					return sumComp;
				return Integer.compare(o1.x, o2.x);
			}
		});
		
		Surface2D floor2 = new Surface2D(new Comparator<FastXYCompactSleigh.Point2D>() {
			@Override
			public int compare(Point2D o1, Point2D o2) {
				int yComp = Integer.compare(o1.y, o2.y);
				if (yComp != 0)
					return yComp;
				return Integer.compare(o1.x, o2.x);
			}
		});
		
		List<Present> presents2 = clonePresents(presents);
		
		int added1 = 0;
		int added2 = 0;
		while(i < presents.size()) {
			added1 = fillLayerReordering(presents, i, floor);
			added2 = fillLayerReordering(presents2, i, floor2);
			
			int added;
			if (added1 >= added2) {
				floor2.maxPresentHeight = floor.maxPresentHeight;
				added = added1;
			}
			else {
				floor.maxPresentHeight = floor2.maxPresentHeight;
				for (int j = i; j < i + added2; j++) {
					presents.get(j).boundaries = presents2.get(j).boundaries;
				}
				added = added2;
			}
			
			int next = i + added;
			if (layerCount % 10 == 0) {
				System.out.println("Layer: " + layerCount + ", presents: " + added + ", total: " + next);
				int area = 0;
				for (int j = i; j < next; j++) {
					Present p = presents.get(j);
					area += (p.xSize * p.ySize);
				}
				System.out.println("Free area: " + (1000000  - area) + ", didn't fit: " + (presents.get(next).xSize * presents.get(next).ySize));
			}
			
			i += added;
			layerCount++;
			if (i < presents.size()) {
				startNewLayer(floor);
				startNewLayer(floor2);
			}
		}
		
		if (added1 >= added2) {
			return floor.zLevel + floor.maxPresentHeight;
		}
		else
			return floor2.zLevel + floor2.maxPresentHeight;
	}
	
	private int fillLayerReordering(List<Present> presents, int start, Surface2D surface) {
		List<Present> layerOrder = Lists.newArrayList();
		int i = 0;
		int added = 0;
		for (i = start; i < presents.size(); i++) {
			Present present = presents.get(i);
			layerOrder.add(present);
			if (!add(present, surface)) {
				undoLayerWithPresents(layerOrder, surface);
				List<Present> sortedLayer = sortByArea(layerOrder);
				if (insertAll(sortedLayer, surface)) {
					layerOrder = sortedLayer;
				} else {
					undoLayerWithPresents(sortedLayer, surface);
					layerOrder.remove(present);
					if (!insertAllCrap(layerOrder, surface)) {
						throw new RuntimeException("foo!");
					}
	//				if (layerCount == 1) {
	//					try {
	//						viewXY(layerOrder);
	//						BrunoMain.generateCSV(layerOrder);
	//					} catch (IOException e) {
	//						e.printStackTrace();
	//					}
	//					System.exit(0);
	//				}
					int jump = completeInTheMiddle(layerOrder, presents, i, surface);
					added+=jump;
					break;
				}
			}
			added++;
		}
		
		return added;
	}
	
	private int completeInTheMiddle(List<Present> layerOrder, List<Present> presents, int i, Surface2D floor)  {
		Map<Integer, Set<Present>> heights = Maps.newLinkedHashMap();
		for (Present present : layerOrder) {
			int height = present.zSize;
			if (!heights.containsKey(height)) {
				heights.put(height, Sets.<Present>newLinkedHashSet());
			}
			heights.get(height).add(present);
		}				
		
		List<Integer> heightsLowToHigh = Lists.newArrayList(heights.keySet());
		Collections.sort(heightsLowToHigh, Ordering.natural());
		
		Surface2D lowerLevel = floor;
		Map<Integer, Surface2D> freeAtHeight = Maps.newLinkedHashMap();
		for (Integer height : heightsLowToHigh) {
			Surface2D free;
			try {
				free = (Surface2D)lowerLevel.clone();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			free.zLevel = height;
			
			
			for (Present present : heights.get(height)) {
				Point oneBasedInsertionPoint = present.boundaries.get(0);
				Point2D zeroBasedInsertionPoint = new Point2D(oneBasedInsertionPoint.x - 1, oneBasedInsertionPoint.y - 1);
				free.insertionPoints.add(zeroBasedInsertionPoint);
				clear(free, zeroBasedInsertionPoint, present.xSize, present.ySize);
			}
			freeAtHeight.put(height, free);
			lowerLevel = free;
		}
		
		int inserted = 0;
		while(true) {
			Present present = presents.get(i + inserted);
			Point insertionPoint3D = fitsIn3D(heightsLowToHigh, freeAtHeight, present, floor);
			if (insertionPoint3D != null) {
				//System.out.println("iupiiiii! " + insertionPoint3D + " " + present);
				inserted++;
				setBoundaries(present, new Point(insertionPoint3D.x + 1, insertionPoint3D.y + 1, insertionPoint3D.z + 1));
				
				int relativeZ = insertionPoint3D.z - floor.zLevel;
				int heightIndex = Collections.binarySearch(heightsLowToHigh, relativeZ);
				if (heightIndex < 0)
					throw new RuntimeException("foo");
				while(heightsLowToHigh.get(heightIndex) < relativeZ + present.zSize) {
					int height = heightsLowToHigh.get(heightIndex);
					Surface2D surface = freeAtHeight.get(height);
					occupy(surface, insertionPoint3D.x, insertionPoint3D.y, present.xSize, present.ySize);
					Iterator<Point2D> it = surface.insertionPoints.iterator();
					while(it.hasNext()) {
						Point2D next = it.next();
						if (insertionPoint3D.x <= next.x && next.x <= insertionPoint3D.x + present.xSize - 1) {
							if (insertionPoint3D.y <= next.y && next.y <= insertionPoint3D.y + present.ySize - 1) {
								it.remove();
								break;
							}
						}
					}
					heightIndex++;
				}
				
				if (!freeAtHeight.containsKey(relativeZ + present.zSize)) {
					heightIndex--; //First one lower than the current present
					int height = heightsLowToHigh.get(heightIndex);
					Surface2D newLevel;
					try {
						newLevel = (Surface2D)freeAtHeight.get(height).clone();
					} catch(Exception e) {
						throw new RuntimeException(e);
					}
					newLevel.zLevel = relativeZ + present.zSize;
					clear(newLevel, new Point2D(insertionPoint3D.x, insertionPoint3D.y), present.xSize, present.ySize);
				}
				
				//Remove lower levels, we don't want to disorder presents
				Iterator<Integer> hi = heightsLowToHigh.iterator();
				while(hi.hasNext()) {
					Integer next = hi.next();
					if (next < relativeZ) {
						hi.remove();
						freeAtHeight.remove(next);
					}
					else
						break;
				}				
			}
			else
				break;
		}
		
		return inserted;
	}
	
	private void clear(Surface2D surface, Point2D point, int xSize, int ySize) {
		for (int p = point.x + xSize - 1; p >= point.x; p--) {
			for (int q = point.y + ySize - 1; q >= point.y; q--) {
				surface.free(p, q);
			}
		}
	}

	private Point fitsIn3D(List<Integer> heightsLowToHigh, Map<Integer, Surface2D> freeAtHeight, Present nextPresent, Surface2D floor) {
		for (Integer height : heightsLowToHigh) {
			Surface2D surfaceAtHeight = freeAtHeight.get(height);
			nextPresent.rotateMaxMedMin();
			Point fitsIn3DAtLevel = fitsIn3DAtLevel(nextPresent, floor.zLevel, surfaceAtHeight);
			if (fitsIn3DAtLevel != null)
				return fitsIn3DAtLevel;
			nextPresent.rotateMinMaxMed();
			fitsIn3DAtLevel = fitsIn3DAtLevel(nextPresent, floor.zLevel, surfaceAtHeight);
			if (fitsIn3DAtLevel != null)
				return fitsIn3DAtLevel;
			nextPresent.rotateMinMedMax();
			fitsIn3DAtLevel = fitsIn3DAtLevel(nextPresent, floor.zLevel, surfaceAtHeight);
			if (fitsIn3DAtLevel != null)
				return fitsIn3DAtLevel;
		}
				
		return null;
	}
	
	private Point fitsIn3DAtLevel(Present present, int floorZLevel, Surface2D surface) {
		if (surface.zLevel + present.zSize <= surface.maxPresentHeight) {
			Point2D fuck = fitsIn2D(present, surface);
			if (fuck != null)
				return new Point(fuck.x, fuck.y, floorZLevel + surface.zLevel);
		}
		return null;
	}
	
	private Point2D fitsIn2D(Present present, Surface2D surface) {
		Point2D insertionPoint = findBLInsertionPoint(present, surface);
		if (insertionPoint != null) {
			return insertionPoint;
		}
		present.rotate();
		return findBLInsertionPoint(present, surface);
	}

	private boolean insertAll(List<Present> sortedCopy, Surface2D surface) {
		return insertAll(sortedCopy, surface, true);
	}
	
	private boolean insertAll(List<Present> sortedCopy, Surface2D surface, boolean with3dOrientation) {
		for (Present p : sortedCopy) {
			if (!add(p, surface, with3dOrientation)) {
				return false;
			}
		}
		return true;
	}
	
	private void undoLayerWithPresents(List<Present> layer, Surface2D surface) {
		for (Present present : layer) {
			present.boundaries.clear();
		}
		surface.clear();
	}

	private List<Present> sortByArea(List<Present> layer) {
		List<Present> sortedCopy = new ArrayList<>(layer);
		Collections.sort(sortedCopy, new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int areaComp = -Ints.compare(o1.xSize * o1.ySize, o2.xSize
						* o2.ySize);
				if (areaComp != 0)
					return areaComp;
				return -Ints.compare(o1.xSize, o2.xSize);
			}
		});
		return sortedCopy;
	}

	private List<Present> sortByXY(List<Present> layer) {
		List<Present> sortedCopy = new ArrayList<>(layer);
		Collections.sort(sortedCopy, new Comparator<Present>() {
			@Override
			public int compare(Present o1, Present o2) {
				int xComp = -Ints.compare(o1.xSize, o2.xSize);
				if (xComp != 0)
					return xComp;

				return -Ints.compare(o1.ySize, o2.ySize);
			}
		});
		return sortedCopy;
	}

	private boolean add(Present present, Surface2D surface) {
		return add(present, surface, true);		
	}
	
	private boolean add(Present present, Surface2D surface, boolean with3DOrientation) {
		if (with3DOrientation) {
			present.rotateMedMinMax();
		}
		Point2D insertPoint = findBLInsertionPoint(present, surface);
		if (insertPoint == null) {
			present.rotate();
			insertPoint = findBLInsertionPoint(present, surface);
		}
		if (insertPoint != null) {
			insert(present, insertPoint, surface);
			return true;
		}
		return false;
	}

	private Point2D findBLInsertionPoint(Present present, Surface2D surface) {
//		SortedSet<Point2D> candidates = new TreeSet<Point2D>();
//		Map<Point2D,Point2D> newToOriginal = Maps.newLinkedHashMap();
		for (Point2D point : surface.insertionPoints) {
			// try to move down and try to move left (at most only one of those
			// is going to move)
			int newY = point.y;
			while (nextHorizontalLineIsFeasible(surface, point.x, newY, present.xSize)) {
				newY--;
			}
			int newX = point.x;
			while (previousVerticalLineIsFeasible(surface, newX, newY, present.ySize)) {
				newX--;
			}
			Point2D newPoint = new Point2D(newX, newY);
//						candidates.add(newPoint);
//						newToOriginal.put(newPoint, point);
			if (fits(surface, newPoint, present.xSize, present.ySize)) {
				surface.insertionPoints.remove(point);
				return newPoint;
			}
		}
		
//		for (Point2D candidate : candidates) {
//			if (fits(surface, candidate, present.xSize, present.ySize)) {
//				surface.insertionPoints.remove(newToOriginal.get(candidate));
//				return candidate;
//			}
//		}
		return null;
	}

	private boolean previousVerticalLineIsFeasible(Surface2D surface, int x, int y, int height) {
		int prevX = x - 1;
		if (prevX < 0 || y + height - 1 >= 1000)
			return false;

		for (int q = y + height - 1; q >= y; q--) {
			if (surface.occupied(prevX, q)) {
				return false;
			}
		}

		return true;
	}

	private boolean nextHorizontalLineIsFeasible(Surface2D surface, int x, int y, int width) {
		int nextY = y - 1;
		if (nextY < 0 || x + width - 1 >= 1000)
			return false;

		for (int p = x + width - 1; p >= x; p--) {
			if (surface.occupied(p, nextY)) {
				return false;
			}
		}

		return true;
	}

	private boolean fits(Surface2D surface, Point2D point, int xSize, int ySize) {
		if (point.x + xSize - 1 >= 1000 || point.y + ySize - 1 >= 1000)
			return false;

		for (int p = point.x + xSize - 1; p >= point.x; p--) {
			for (int q = point.y + ySize - 1; q >= point.y; q--) {
				if (surface.occupied(p, q)) {
					return false;
				}
			}
		}
		return true;
	}

	private void startNewLayer(Surface2D surface) {
		surface.zLevel += surface.maxPresentHeight;
		surface.clear();
	}

	private void insert(Present present, Point2D insertionPoint, Surface2D surface) {
		this.occupy(surface, insertionPoint.x, insertionPoint.y, present.xSize,
				present.ySize);

		// Might not be present if we moved down or left, so we have to find the one to remove
//		if (!insertionPoints.remove(insertionPoint)) {
//			Iterator<Point2D> i = insertionPoints.iterator();
//			while(i.hasNext()) {
//				Point2D next = i.next();
//				if (insertionPoint.x <= next.x && next.x <= insertionPoint.x + present.xSize - 1) {
//					if (insertionPoint.y <= next.y && next.y <= insertionPoint.y + present.ySize - 1) {
//						i.remove();
//						break;
//					}
//				}
//			}
//		}
		
		int xNewBottomRight = insertionPoint.x + present.xSize;
		if (xNewBottomRight < 1000 && !surface.occupied(xNewBottomRight, insertionPoint.y)) {
			surface.insertionPoints.add(new Point2D(xNewBottomRight, insertionPoint.y));
		}
		int yNewTopLeft = insertionPoint.y + present.ySize;
		if (yNewTopLeft < 1000 && !surface.occupied(insertionPoint.x, yNewTopLeft)) {
			surface.insertionPoints.add(new Point2D(insertionPoint.x, yNewTopLeft));
		}

		Point oneBasedInsertionPoint = new Point(insertionPoint.x + 1, insertionPoint.y + 1, surface.zLevel + 1);
		setBoundaries(present, oneBasedInsertionPoint);

		if (present.zSize > surface.maxPresentHeight) {
			surface.maxPresentHeight = present.zSize;
		}

		// System.out.println(present.order + " [" + present.xSize + ","+
		// present.ySize +","+ present.zSize +"] "+ oneBasedInsertionPoint);
	}

	private void setBoundaries(Present present, Point insertPoint) {
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y,
				insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y
				+ present.ySize - 1, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y, insertPoint.z));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y + present.ySize - 1, insertPoint.z));

		present.boundaries.add(new Point(insertPoint.x, insertPoint.y,
				insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x, insertPoint.y
				+ present.ySize - 1, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y, insertPoint.z + present.zSize - 1));
		present.boundaries.add(new Point(insertPoint.x + present.xSize - 1,
				insertPoint.y + present.ySize - 1, insertPoint.z
						+ present.zSize - 1));
	}

	private void occupy(Surface2D surface, int x, int y, int xSize, int ySize) {
		for (int p = x; p < x + xSize; p++) {
			for (int q = y; q < y + ySize; q++) {
				surface.occupy(p, q);
			}
		}
	}
	
	/* **************** DEBUG STUF ****************** */
	
	private void viewXY(List<Present> presents) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Present p : presents) {
			System.out.println(p.order + " (" + p.xSize + "," + p.ySize + "): " + p.boundaries.get(0).x + " " + p.boundaries.get(0).y);
			if (p.order < min)
				min = p.order;
			if (p.order > max)
				max = p.order;
		}
		System.out.println("min " + min + " max " + max);
	}
	
	private boolean insertAllCrap(List<Present> sortedCopy, Surface2D surface) {
		for (Present p : sortedCopy) {
//			if (p.order == 55484) {
//				exportInsertionPoints(surface);
//				export(surface);
//			}
			if (!add(p, surface)) {
				return false;
			}
		}
		return true;
	}
	
	public void exportInsertionPoints(Surface2D surface) {
		System.out.println("exporting insertion points...");
		try {
			CSVWriter w = new CSVWriter(new FileWriter(new File("brunoLayerIP.csv")), ',',
					CSVWriter.NO_QUOTE_CHARACTER);
			for (Point2D p : surface.insertionPoints) {
				String[] line = new String[2];
				line[0] = String.valueOf(p.x);
				line[1] = String.valueOf(p.y);
				w.writeNext(line);
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done.");
	}
	
	public void export(Surface2D surface) {
		System.out.println("exporting...");
		try {
			CSVWriter w = new CSVWriter(new FileWriter(new File("brunoLayer.csv")), ',',
					CSVWriter.NO_QUOTE_CHARACTER);
			for (int xi = 0; xi < 1000; xi++) {
				for (int yi = 0; yi < 1000; yi++) {
					if (surface.occupied(xi, yi)) {
						String[] line = new String[2];
						line[0] = String.valueOf(xi);
						line[1] = String.valueOf(yi);
						w.writeNext(line);
					}
				}
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done.");
	}

}
