package org.collectionspace.services.structureddate.antlr;

/**
 * A deferred date that represents an endpoint of a century.
 */
public abstract class DeferredCenturyDate extends DeferredDate {
	protected int century;

	public DeferredCenturyDate(int century) {
		this.century = century;
	}
}
