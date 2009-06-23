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

public class IDPattern {

	final static int MAX_ID_LENGTH = 30;
	
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
		// call its getNextID() method, and 
		int last = this.parts.size() - 1;
		this.parts.get(last).getNextID();
		// lastPart.getNextID();
		// this.parts.set(last, lastPart);
		for (IDPart part : this.parts) {
			sb.append(part.getCurrentID());
		}
		return sb.toString();
	}

	// public boolean validate() {};
 
}
