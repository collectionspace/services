package org.collectionspace.services.structureddate;

/**
 * A deferred date that represents an endpoint of a partial decade.
 */
public abstract class DeferredPartialDecadeDate extends DeferredDecadeDate {
	protected Part part;
	
	public DeferredPartialDecadeDate(int decade) {
		super(decade);
	}

	public DeferredPartialDecadeDate(int decade, Part part) {
		this(decade);
		
		this.part = part;
	}
}
