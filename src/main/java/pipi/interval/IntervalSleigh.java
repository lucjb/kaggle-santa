package pipi.interval;

import java.util.List;

import pipi.Box3d;
import pipi.Dimension2d;
import pipi.OrientedDimension3d;
import pipi.OutputPresent;
import pipi.PresentBatch;
import pipi.SuperPresent;
import pipi.sleigh.FloorStructure;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import first.Point;

public class IntervalSleigh {

	private List<OutputPresent> outputPresents = Lists.newArrayList();

	public IntervalSleigh() {
	}

	public void emitPresents(List<PutRectangle> list, Multimap<OrientedDimension3d, SuperPresent> multimap,
			FloorStructure floorStructure) {
		List<OutputPresent> newOutputPresents = Lists.newArrayList();

		PresentBatch presentBatch = new PresentBatch(Integer.MAX_VALUE);
		for (PutRectangle putRectangle : list) {
			Rectangle rectangle = putRectangle.rectangle;
			OrientedDimension3d orientedDimension3d = new OrientedDimension3d(rectangle.box2d.dimension(),
					putRectangle.height);
			SuperPresent present = multimap.get(orientedDimension3d).iterator().next();
			multimap.remove(orientedDimension3d, present);
			Box3d box3d = new Box3d(rectangle.box2d.dx, rectangle.box2d.dy, putRectangle.height);

			newOutputPresents.add(new OutputPresent(present.getOrder(), new Point(rectangle.point2d.x, rectangle.point2d.y,
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
