package pipi;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SuperPresent {

	private final int order;
	private final PresentDimension dimension;

	public SuperPresent(int order, PresentDimension dimension) {
		this.order = order;
		this.dimension = dimension;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	public int getOrder() {
		return this.order;
	}
	
	public PresentDimension getDimension() {
		return this.dimension;
	}
	
}
