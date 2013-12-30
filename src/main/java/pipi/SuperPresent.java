package pipi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SuperPresent {

	private final int order;
	private final Dimension3d dimension;

	public SuperPresent(int order, Dimension3d dimension) {
		this.order = order;
		this.dimension = dimension;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	public int getOrder() {
		return this.order;
	}
	
	public Dimension3d getDimension() {
		return this.dimension;
	}
	
}
