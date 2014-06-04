package org.collectionspace.services.structureddate;

public abstract class DeferredHalfCenturyDate extends DeferredCenturyDate {
	protected int half;
	
	public DeferredHalfCenturyDate(int century) {
		super(century);
	}

	public DeferredHalfCenturyDate(int century, int half) {
		this(century);
		
		this.half = half;
	}
}
