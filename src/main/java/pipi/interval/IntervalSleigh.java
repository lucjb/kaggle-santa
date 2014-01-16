package pipi.interval;

import java.util.List;

import pipi.Box3d;
import pipi.Dimension2d;
import pipi.OutputPresent;
import pipi.SuperPresent;
import pipi.sleigh.FloorStructure;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import first.Point;

public class IntervalSleigh {

	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalSleigh() {
	}

	public void emitPresents(List<Rectangle> putPesent, Multimap<Dimension2d, SuperPresent> presentsWithDimension,
			FloorStructure floorStructure) {
		List<OutputPresent> newOutputPresents = Lists.newArrayList();

		for (Rectangle rectangle : putPesent) {
			Dimension2d dimension = rectangle.getBox2d().dimension();
			SuperPresent present = presentsWithDimension.get(dimension).iterator().next();
			presentsWithDimension.remove(dimension, present);
			Rectangle min = rectangle;
			Box3d box3d = new Box3d(rectangle.box2d.dx, rectangle.box2d.dy, present.getDimension().large);

			newOutputPresents.add(new OutputPresent(present.getOrder(), new Point(min.point2d.x, min.point2d.y,
					floorStructure.getCurrentZ()), box3d));
		}
		this.outputPresents.addAll(newOutputPresents);
		for (OutputPresent outputPresent : newOutputPresents) {
			int height = outputPresent.getPoint().z + outputPresent.getBox().dz;
			Rectangle newRectangle = Rectangle.of(outputPresent.getPoint().x, outputPresent.getPoint().y,
					outputPresent.getBox().dx, outputPresent.getBox().dy);
			floorStructure.putPresent(height, newRectangle);
		}

	}

	public List<OutputPresent> getOutputPresents() {
		return this.outputPresents;
	}

}
