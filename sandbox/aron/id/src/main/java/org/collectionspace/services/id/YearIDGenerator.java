/*	
 * YearIDGenerator
 *
 * <p>An identifier generator that stores and returns the current year
 * as a String object.</p>
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

// @TODO: Need to understand and reflect time zone issues.

package org.collectionspace.services.id;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class YearIDGenerator implements IDGenerator {
    
	private String initialValue = null;
	private String currentValue = null;
	
	public YearIDGenerator() throws IllegalArgumentException {

		String currentYear = getCurrentYear();
		this.initialValue = currentYear;
		this.currentValue = currentYear;

	}
	
	public YearIDGenerator(String initialValue) throws IllegalArgumentException {

		if ( initialValue == null ) {
			throw new IllegalArgumentException("Initial value must not be null");
		}
	
		if ( initialValue == "" ) {
			throw new IllegalArgumentException("Initial value must not be empty");
		}
		
		// @TODO: Add regex-based validation here, by calling a
		// to-be-added validate() method
		
		this.initialValue = initialValue;
		this.currentValue = initialValue;

	}

	public synchronized void reset() {
		this.currentValue = this.initialValue;
	}

	public synchronized String getInitialID() {
		return this.initialValue;
	}

	public synchronized String getCurrentID() {
		return this.currentValue;
	}
	
	// @TODO: We'll need to decide what a "next" ID means in the context of:
	// * An initially supplied value.
	// * A year value that has not changed from its previous value.
	// * A year value that has changed, as a result of a rollover
	//   to a new instant in time.
	public synchronized String getNextID() {
		return this.currentValue;
  }

	public String getCurrentYear() {
		Calendar cal = GregorianCalendar.getInstance();
    int y = cal.get(Calendar.YEAR);
		return Integer.toString(y);
	}	
	
}
