package pipi;

public class QueuedPresent {

	public OrientedDimension3d orientedDimension3d;
	public int orientation;
	public Dimension3d dimension;

	public QueuedPresent(Dimension3d dimension, OrientedDimension3d smallOriented, int orientation) {
		this.dimension = dimension;
		this.orientedDimension3d = smallOriented;
		this.orientation = orientation;
	}
	
	@Override
	public String toString() {
		return this.orientedDimension3d.toString();
	}

}
