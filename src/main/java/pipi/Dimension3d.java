package pipi;

import java.util.Arrays;

public class Dimension3d {

	private final int[] dimensions;

	private Dimension3d(int[] array) {
		this.dimensions = array;
	}

	public static Dimension3d create(int i, int j, int k) {
		int[] array = new int[] { i, j, k };
		Arrays.sort(array);
		return new Dimension3d(array);
	}

	@Override
	public String toString() {
		return String.format("%dx%dx%d", this.getSmall(), this.getMedium(), this.getLarge());
	}

	public Dimension2d smallFace() {
		return new Dimension2d(this.getSmall(), this.getMedium());
	}

	public Dimension2d mediumFace() {
		return new Dimension2d(this.getSmall(), this.getLarge());
	}

	public Dimension2d largeFace() {
		return new Dimension2d(this.getMedium(), this.getLarge());
	}

	public OrientedDimension3d getRotation(int rotation) {
		return new OrientedDimension3d(this.getFace(rotation), this.getOpposingDimension(rotation));
	}
	
	private static int[] FACE_SMALL = { 0, 0, 1 };
	private static int[] FACE_LARGE = { 1, 2, 2 };

	public Dimension2d getFace(int face) {
		return new Dimension2d(this.getDimension(FACE_SMALL[face]), this.getDimension(FACE_LARGE[face]));
	}

	public int getDimension(int dimension) {
		return this.dimensions[dimension];
	}

	public int getOpposingDimension(int dimension) {
		return this.getDimension(2 - dimension);
	}

	public int getLarge() {
		return this.dimensions[2];
	}

	public int getMedium() {
		return this.dimensions[1];
	}

	public int getSmall() {
		return this.dimensions[0];
	}

}
