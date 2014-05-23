package org.collectionspace.services.structureddate;

public abstract class DeferredMillenniumDate extends DeferredDate {
	protected int millennium;
	
	public DeferredMillenniumDate(int millennium) {
		this.millennium = millennium;
	}
}
