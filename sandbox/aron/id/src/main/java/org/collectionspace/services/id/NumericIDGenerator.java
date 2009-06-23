/*	
 * NumericIDGenerator
 *
 * <p>An identifier generator that generates an incrementing ID as a
 * series of numeric values, beginning from an initial value.</p>
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */

// @TODO: Add Javadoc comments

// @TODO: Need to set and enforce maximum value.
 
package org.collectionspace.services.id;

public class NumericIDGenerator implements IDGenerator {
    
  private long initialValue = 0;
	private long currentValue = 0;

	public NumericIDGenerator(String initialValue) throws IllegalArgumentException {
		try {
			long l = Long.parseLong(initialValue.trim());
			if ( l < 0 ) {
				throw new IllegalArgumentException("Initial ID value should be zero (0) or greater");
			}
			this.currentValue = l;
			this.initialValue = l;
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Initial ID value should not be null");
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Initial ID value must be parseable as a number");
		}
	}

	public synchronized void reset() {
		this.currentValue = this.initialValue;
	}

	public synchronized String getInitialID() {
		return Long.toString(this.initialValue);
	}

	public synchronized String getCurrentID() {
		return Long.toString(this.currentValue);
	}
	
	public synchronized String getNextID() {
		this.currentValue++;
		return Long.toString(this.currentValue);
	}

	public synchronized boolean isValidID(String value) throws IllegalArgumentException {
		// Currently stubbed-out
		return true;
	}
	
}
