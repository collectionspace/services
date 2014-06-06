package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents an endpoint of a millennium.
 */
public abstract class DeferredMillenniumDate extends DeferredDate {
	protected int millennium;
	
	public DeferredMillenniumDate(int millennium) {
		this.millennium = millennium;
	}
}
