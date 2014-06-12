package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents an endpoint of a half century.
 */
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
