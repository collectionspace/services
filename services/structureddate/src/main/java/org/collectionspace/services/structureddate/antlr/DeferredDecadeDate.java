package org.collectionspace.services.structureddate.antlr;

/**
 * A deferred date that represents an endpoint of a decade.
 */
public abstract class DeferredDecadeDate extends DeferredDate {
	protected Integer decade;
	
	public DeferredDecadeDate(Integer decade) {
		this.decade = decade;
	}
}
