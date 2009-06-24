 /*	
 * IDPattern
 *
 * <p>Models an identifier (ID), which consists of multiple IDParts.</p>
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

	// Returns the next value of this ID.
	public synchronized String getNextID() {
		StringBuffer sb = new StringBuffer(MAX_ID_LENGTH);
		// Obtain the last (least significant) IDPart,
		// and call its getNextID() method, which will
		// concurrently set the current value of that ID
		// to the next ID.
		int last = this.parts.size() - 1;
		this.parts.get(last).getNextID();
		// Then call the getCurrentID() method on all of the IDParts
		for (IDPart part : this.parts) {
			sb.append(part.getCurrentID());
		}
		return sb.toString();
	}

	// Validates a provided ID against the pattern.
	public synchronized boolean isValidID(String value) {
	
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
