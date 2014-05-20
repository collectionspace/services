package org.collectionspace.services.structureddate.antlr;

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
