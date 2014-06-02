package org.collectionspace.services.structureddate.antlr;

import org.collectionspace.services.structureddate.Date;

/**
 * Represents a date that is not completely known, and whose
 * unknown parts require some future calculation to be determined.
 */
public abstract class DeferredDate extends Date {
	
	/**
	 * Finalizes the date by executing the deferred
	 * calculation. This causes all of the date parts
	 * to be determined.
	 */
	public abstract void finalizeDate();
}
