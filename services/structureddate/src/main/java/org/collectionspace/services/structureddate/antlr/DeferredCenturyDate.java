package org.collectionspace.services.structureddate.antlr;

/**
 * A deferred date that represents an endpoint of a century.
 */
public abstract class DeferredCenturyDate extends DeferredDate {
	protected Integer century;

	public DeferredCenturyDate(Integer century) {
		this.century = century;
	}
}
