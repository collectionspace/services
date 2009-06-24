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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericIDGenerator implements IDGenerator {
  
  final static private int DEFAULT_MAX_LENGTH = 6;
  private int maxLength = DEFAULT_MAX_LENGTH;
  
  private long initialValue = 0;
	private long currentValue = 0;

	public NumericIDGenerator(String initialValue) throws IllegalArgumentException {
		this(initialValue, Integer.toString(DEFAULT_MAX_LENGTH));
	}

	public NumericIDGenerator(String initialValue, String maxLength)
		throws IllegalArgumentException {
		
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

		try {
			this.maxLength = Integer.parseInt(maxLength);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Maximum ID length must be parseable as a number");
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

  // Sets the current value.
	public synchronized void setCurrentID(String value) throws IllegalArgumentException {

	  // @TODO Much of this code is copied from the main constructor,
	  // and may be ripe for refactoring.
		try {
			long l = Long.parseLong(value.trim());
			if ( l < 0 ) {
				throw new IllegalArgumentException("Initial ID value should be zero (0) or greater");
			}
			this.currentValue = l;
			this.initialValue = l;
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("ID value should not be null");
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("ID value must be parseable as a number");
		}
		
		// @TODO An expedient; we may need to check the String length of the
		// provided ID and calculate a maximum length here.
		this.maxLength = DEFAULT_MAX_LENGTH;
	}
	
	public synchronized String getNextID() throws IllegalStateException {
		this.currentValue++;
		String nextID = Long.toString(this.currentValue);
		if (nextID.length() > this.maxLength) {
			throw new IllegalStateException("Next ID cannot exceed maximum length");
		}
		return nextID;
	}

	public synchronized boolean isValidID(String value) {

		if ( value == null || value == "") {
			return false;
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
		String regex = "(" + "\\d" + "{1," + Integer.toString(this.maxLength) + "}" + ")";
		return regex;
	}
	
}
