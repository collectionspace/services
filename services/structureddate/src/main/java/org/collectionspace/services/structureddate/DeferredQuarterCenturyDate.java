package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents an endpoint of a quarter century.
 */
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
