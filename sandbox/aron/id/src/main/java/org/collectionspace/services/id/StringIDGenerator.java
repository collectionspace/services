/*	
 * StringIDGenerator
 *
 * <p>An identifier generator that stores and returns a static String.</p>
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

// @TODO: Need to set and enforce maximum String length.

package org.collectionspace.services.id;

public class StringIDGenerator implements IDGenerator {
    
	private String initialValue = null;
	private String currentValue = null;
	
	public StringIDGenerator(String initialValue) throws IllegalArgumentException {

		if ( initialValue == null || initialValue == "") {
			throw new IllegalArgumentException("Initial value must not be null or empty");
		}
		
		this.initialValue = initialValue;
		this.currentValue = initialValue;

	}

	public synchronized void reset() {
		// Do nothing
	}

	public synchronized String getInitialID() {
		return this.initialValue;
	}

	public synchronized String getCurrentID() {
		return this.currentValue;
	}
	
	public synchronized String getNextID() {
		return this.currentValue;
  }
	
}
