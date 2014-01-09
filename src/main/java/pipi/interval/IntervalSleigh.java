package pipi.interval;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.xml.crypto.KeySelector.Purpose;

import pipi.Box3d;
import pipi.Dimension2d;
import pipi.OrientedDimension3d;
import pipi.OutputPresent;
import pipi.PresentBatch;
import pipi.SuperPresent;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import first.Point;

public class IntervalSleigh {

	private int currentZ;
	private int lastZ = 0;
	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalSleigh() {
		this.currentZ = 0;
	}

	public void emitPresents(List<Rectangle> putPesent, Multimap<Dimension2d, SuperPresent> presentsWithDimension,
			PriorityQueue<ExtendedRectangle> carryRectangles2) {
		int maxZ = this.currentZ;
//		PresentBatch presentBatch = new PresentBatch();
		// PriorityQueue<Box3d> carryRectangles = new
		// PriorityQueue<>(putPesent.size(), new Comparator<Box3d>() {
		//
		// @Override
		// public int compare(Box3d o1, Box3d o2) {
		// return Ints.compare(o1.dy, o2.dy);
		// }
		// });
		// int minZ = Integer.MAX_VALUE;

		List<OutputPresent> newOutputPresents = Lists.newArrayList();

		for (Rectangle rectangle : putPesent) {
			Dimension2d dimension = rectangle.getBox2d().dimension();
			SuperPresent present = presentsWithDimension.get(dimension).iterator().next();
			presentsWithDimension.remove(dimension, present);
			Rectangle min = rectangle;
			Box3d box3d = new Box3d(rectangle.box2d.dx, rectangle.box2d.dy, present.getDimension().large);

			newOutputPresents.add(new OutputPresent(present.getOrder(), new Point(min.point2d.x, min.point2d.y,
					this.currentZ), box3d));
			int topZ = this.currentZ + box3d.dz;
			// carryRectangles.add(new Box3d(box3d.dx, box3d.dy, topZ));

			maxZ = Math.max(maxZ, topZ);
			// minZ = Math.min(minZ, topZ);
//			presentBatch.pushPresent(present.getDimension(), 0);
		}
		this.outputPresents.addAll(newOutputPresents);
		for (OutputPresent outputPresent : newOutputPresents) {
			int height = outputPresent.getPoint().z + outputPresent.getBox().dz;
			Rectangle newRectangle = Rectangle.of(outputPresent.getPoint().x, outputPresent.getPoint().y,
					outputPresent.getBox().dx, outputPresent.getBox().dy);
			carryRectangles2.add(new ExtendedRectangle(newRectangle, height));
		}

		this.currentZ = carryRectangles2.peek().height;
		while(carryRectangles2.peek().height == this.currentZ){
			carryRectangles2.remove();
		}

		
//		System.out.println("After-->" + presentBatch);
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
