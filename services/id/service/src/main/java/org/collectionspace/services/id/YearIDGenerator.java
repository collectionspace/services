/*	
 * YearIDGenerator
 *
 * Generates identifiers (IDs) that store and returns the current year
 * or a supplied year as a String object.
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */

// @TODO: Add Javadoc comments

// @TODO: Need to understand and reflect time zone issues;
// what happens when a year rollover occurs:
// - In the time zone of the end user.
// - In the time zone of the museum or other institution.
// - In the time zone of the physical server where the code is hosted.

// NOTE: This class currently hard-codes the assumption that the
// Gregorian Calendar system is in use.
//
// We may wish to use the Joda-Time framework if handling of
// additional calendar systems is needed, or additional treatment
// of time zones is warranted:
// http://joda-time.sourceforge.net/
//
// There may also be a need to have a structured set of date-time
// classes related to identifier generation.

package org.collectionspace.services.id;

import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearIDGenerator implements IDGenerator {
    
	private String initialValue = null;
	private String currentValue = null;
	
	public YearIDGenerator() throws IllegalArgumentException {

		String currentYear = getCurrentYear();
		this.initialValue = currentYear;
		this.currentValue = currentYear;

	}
	
	public YearIDGenerator(String initialValue) throws IllegalArgumentException {

		if ( initialValue == null || initialValue == "") {
			throw new IllegalArgumentException("Initial ID value must not be null or empty");
		}
		
		// @TODO: Add regex-based validation here, by calling isValidID().
		// Consider implications for Internationalization when doing so.
		
		this.initialValue = initialValue;
		this.currentValue = initialValue;

	}

	public synchronized String getInitialID() {
		return this.initialValue;
	}

	public synchronized String getCurrentID() {
		return this.currentValue;
	}

  // Sets the current value.
	public synchronized void setCurrentID(String value) throws IllegalArgumentException {

	  // @TODO This code is copied from the main constructor,
	  // and thus there may be an opportunity for refactoring.

		if ( value == null || value == "") {
			throw new IllegalArgumentException("ID value must not be null or empty");
		}
		
		// @TODO: Add regex-based validation here, by calling isValidID().
		// Consider implications for Internationalization when doing so.

		this.currentValue = value;

	}
	
	public synchronized void resetID() {
		this.currentValue = this.initialValue;
	}

	// @TODO: We'll need to decide what a "next" ID means in the context of:
	// - An initially supplied value.
	// - A year value that has not changed from its previous value.
	// - A year value that has changed, as a result of a rollover
	//   to a new instant in time.
	public synchronized String nextID() {
		return this.currentValue;
  }

	public String getCurrentYear() {
		Calendar cal = GregorianCalendar.getInstance();
    int y = cal.get(Calendar.YEAR);
		return Integer.toString(y);
	}	

	public synchronized boolean isValidID(String value) throws IllegalArgumentException {

		if ( value == null || value == "") {
			throw new IllegalArgumentException("ID to validate must not be null or empty");
		}

		Pattern pattern = Pattern.compile(getRegex());
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
		
	}

	public synchronized String getRegex() {
		// NOTE: Currently hard-coded to accept only a range of
		// four-digit Gregorian Calendar year dates.
		String regex = "(\\d{4})";
		return regex;
	}
	
}
