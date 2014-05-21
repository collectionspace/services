package org.collectionspace.services.structureddate;

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
