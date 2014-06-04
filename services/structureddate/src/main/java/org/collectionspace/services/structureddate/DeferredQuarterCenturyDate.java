package org.collectionspace.services.structureddate;

public abstract class DeferredQuarterCenturyDate extends DeferredCenturyDate {
	protected int quarter;
	
	public DeferredQuarterCenturyDate(int century) {
		super(century);
	}

	public DeferredQuarterCenturyDate(int century, int quarter) {
		this(century);
		
		this.quarter = quarter;
	}
}
