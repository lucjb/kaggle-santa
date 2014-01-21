package pipi;

public class OrientedDimension3d {

	public final Dimension2d base;
	public final int height;

	public OrientedDimension3d(Dimension2d base, int height) {
		this.base = base;
		this.height = height;
	}

	public int volume() {
		return this.base.area() * this.height;
	}
	
	@Override
	public String toString() {
		return this.base + "|"  + this.height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.base == null) ? 0 : this.base.hashCode());
		result = prime * result + this.height;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		OrientedDimension3d other = (OrientedDimension3d) obj;
		if (this.base == null) {
			if (other.base != null)
				return false;
		} else if (!this.base.equals(other.base))
			return false;
		if (this.height != other.height)
			return false;
		return true;
	}
	
	
}
