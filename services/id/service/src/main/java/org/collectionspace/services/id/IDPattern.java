 /*	
 * IDPattern
 *
 * Models an identifier (ID), which consists of multiple IDParts.
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

// @TODO: Catch Exceptions thrown by IDPart, then
// reflect this in the corresponding IDPatternTest class.

package org.collectionspace.services.id;

import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDPattern {

	final static int MAX_ID_LENGTH = 50;
	
	private Vector<IDPart> parts = new Vector<IDPart>();

	// Constructor
	public IDPattern() {
	}
	
	// Constructor
	public IDPattern(Vector<IDPart> partsList) {
		if (partsList != null) {
			this.parts = partsList;
		}
	}

	public void add(IDPart part) {
		if (part != null) {
			this.parts.add(part);
		}
	}

	// Returns the current value of this ID.
	public synchronized String getCurrentID() {
		StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
		for (IDPart part : this.parts) {
			sb.append(part.getCurrentID());
		}
		return sb.toString();
	}

	// Returns the current value of this ID, given a
	// supplied ID that partly matches the pattern.
	//
	// If the supplied ID fully matches the pattern,
	// will return the supplied ID.
	//
	// However, if the supplied ID is a partial ID, which
	// partly "stem-matches" the pattern but does not
	// ully match the pattern, will return the partial ID with
	// its next ID component appended.  The next ID component
	// will be set to its initial value.
	//
	// Examples:
	// * 2009.5." becomes "2009.5.1", in a case where the
	//   next ID component is an incrementing numeric IDPart.
	// * "E55-" becomes "E55-a", where the next ID component
	//   is an incrementing alphabetic IDPart.
	public synchronized String getCurrentID(String value)
		throws IllegalArgumentException {

	  if (value == null) return value;
	  
	  // Try ever-larger stem matches against the supplied value,
	  // by incrementally appending each part's regex, until no
	  // (more) matches are found.
	  //
	  // In so doing, build a subset of this IDPattern's regex
	  // that fully matches the supplied value.
	  Pattern pattern = null;
	  Matcher matcher = null;
	  int matchedParts = 0;
	  StringBuffer regexToTry = new StringBuffer();
	  StringBuffer regex = new StringBuffer();
	  for (IDPart partToTryMatching : this.parts) {
	  	regexToTry.append(partToTryMatching.getRegex());
	  	pattern = Pattern.compile(regexToTry.toString());
			matcher = pattern.matcher(value);
			// If a stem match was found on the current regex,
			// store a count of matched IDParts and the regex pattern
			// that has matched to this point.
			if (matcher.lookingAt()) {
				matchedParts++;
	  		regex.append(partToTryMatching.getRegex());
			// Otherwise, exit the loop.
			} else {
				break;
			}
		}

		// If the supplied ID doesn't partly match the pattern,
		// throw an Exception.
		if (matchedParts == 0) {
			throw new IllegalArgumentException("Supplied ID does not match this ID pattern.");
		}

		pattern = Pattern.compile(regex.toString());
		matcher = pattern.matcher(value);
		
		// If the supplied ID doesn't match the pattern built above,
		// throw an Exception.  (This error condition should likely
		// never be reached, but it's here as a guard.)
		if (! matcher.matches()) {
			throw new IllegalArgumentException("Supplied ID does not match this ID pattern.");
		}
		
		// Otherwise, if the supplied ID matches the pattern,
		// split the ID into its components and store those
		// values in each of the pattern's IDParts.
		IDPart currentPart;
		for (int i = 1; i <= matchedParts; i++) {
		  currentPart = this.parts.get(i - 1);
      currentPart.setCurrentID(matcher.group(i));
		}

		// Obtain the initial value of the next IDPart, and
		// set the current value of that part to its initial value.
		//
		// If the supplied ID fully matches the pattern, there will
		// be no 'next' IDPart, and we must catch that Exception below. 
		int nextPartNum = matchedParts;
		try {
			String initial = this.parts.get(nextPartNum).getInitialID();
			this.parts.get(nextPartNum).setCurrentID(initial);
			// Increment the number of matched parts to reflect the
			// addition of this next IDPart.
			matchedParts++;
		} catch (ArrayIndexOutOfBoundsException e ) {
			// Do nothing here; we simply won't increment
			// the number of matched parts, used in the loop below.
		}
		
		// Call the getCurrentID() method on each of the
		// supplied IDParts, as well as on the added IDPart
		// whose initial value was just obtained, if any.
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i <= matchedParts; i++) {
			sb.append(this.parts.get(i - 1).getCurrentID());
		}
		
		return sb.toString();

	}

	// Returns the next value of this ID, and sets the current value to that ID.
	public synchronized String nextID() throws IllegalStateException {
	
		// Obtain the last (least significant) IDPart,
		// and call its nextID() method, which will
		// concurrently set the current value of that ID
		// to the next ID.
		int lastPartNum = this.parts.size() - 1;
		this.parts.get(lastPartNum).nextID();
		
		// Then call the getCurrentID() method on all of the IDParts
		StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
		for (IDPart part : this.parts) {
			sb.append(part.getCurrentID());
		}
		
		return sb.toString();
		
	}

	// Returns the next value of this ID, given a
	// supplied ID that entirely matches the pattern,
	// and sets the current value to that ID.
	public synchronized String nextID(String value)
		throws IllegalStateException, IllegalArgumentException {

	  if (value == null) { 
	  	throw new IllegalArgumentException("Supplied ID cannot be null.");
	  }
	
		Pattern pattern = Pattern.compile(getRegex());
		Matcher matcher = pattern.matcher(value);
		
		// If the supplied ID doesn't entirely match the pattern,
		// throw an Exception.
		if (! matcher.matches()) {
			throw new IllegalArgumentException("Supplied ID does not match this ID pattern.");
		}
		
		// Otherwise, if the supplied ID entirely matches the pattern,
		// split the ID into its components and store those values in
		// each of the pattern's IDParts.
		IDPart currentPart;
		for (int i = 1; i <= (matcher.groupCount() - 1); i++) {
		  currentPart = this.parts.get(i - 1);
      currentPart.setCurrentID(matcher.group(i));
		}

		// Obtain the last (least significant) IDPart,
		// and call its nextID() method, which will
		// concurrently set the current value of that ID
		// to the next ID.
		//
		// @TODO: This code is duplicated in nextID(), above,
		// and thus we may want to refactor this.
		int lastPartNum = this.parts.size() - 1;
		this.parts.get(lastPartNum).nextID();
		
		// Then call the getCurrentID() method on all of the IDParts
		StringBuffer sb = new StringBuffer();
		for (IDPart part : this.parts) {
			sb.append(part.getCurrentID());
		}
		
		return sb.toString();
		
	}

	// Validates a provided ID against the pattern.
	//
	// @TODO May potentially throw at least one pattern-related exception;
	// we'll need to catch and handle this.
	public synchronized boolean isValidID(String value) {
	
	  if (value == null) return false;
	
		Pattern pattern = Pattern.compile(getRegex());
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
		
	}

	// Returns a regular expression to validate this ID.
	public synchronized String getRegex() {
		StringBuffer sb = new StringBuffer();
		for (IDPart part : this.parts) {
			sb.append(part.getRegex());
		}
		return sb.toString();
	}
 
}
