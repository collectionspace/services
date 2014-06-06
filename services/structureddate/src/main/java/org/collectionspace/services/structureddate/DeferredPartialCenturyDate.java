package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents an endpoint of a partial century.
 */
public abstract class DeferredPartialCenturyDate extends DeferredCenturyDate {
	protected Part part;
	
	public DeferredPartialCenturyDate(int century) {
		super(century);
	}

	public DeferredPartialCenturyDate(int century, Part part) {
		this(century);
		
		this.part = part;
	}
}
