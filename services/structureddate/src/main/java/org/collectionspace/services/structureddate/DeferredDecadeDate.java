package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents an endpoint of a decade.
 */
public abstract class DeferredDecadeDate extends DeferredDate {
	protected int decade;
	
	public DeferredDecadeDate(int decade) {
		this.decade = decade;
	}
}
