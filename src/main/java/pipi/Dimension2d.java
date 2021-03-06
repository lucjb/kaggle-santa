package pipi;


public class Dimension2d {

	public final int small;
	public final int large;

	public Dimension2d(int small, int large) {
		assert small <= large;
		this.small = small;
		this.large = large;
	}

	public static Dimension2d of(int i, int j) {
		return new Dimension2d(Math.min(i,j), Math.max(i,j));
	}

	@Override
	public String toString() {
		return String.format("%dx%d", this.small, this.large);
	}

	public boolean contains(Dimension2d base) {
		return this.small >= base.small && this.large >= base.large;
	}

	public Box2d horizontal() {
		return new Box2d(this.large, this.small);
	}

	public Box2d vertical() {
		return new Box2d(this.small, this.large);
	}

	public int area() {
		return this.small * this.large;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.large;
		result = prime * result + this.small;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Dimension2d other = (Dimension2d) obj;
		return this.large == other.large & this.small == other.small;
	}
	
	public float squareness(){
		return (float)this.small / this.large; 
	}
	
	public int perimeter(){
		return this.small + this.small + this.large + this.large;
	}
	
	
	
	
}
