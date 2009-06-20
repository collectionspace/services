 /*	
 * IDPart
 *
 * Models a part of an identifier (ID), such as (for instance) an incrementing
 * numeric or alphabetic value, a date value, or a static separator.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author$
 * @version $Revision$
 * $Date$
 */
 
package org.collectionspace.services.id;

import org.apache.commons.id.StringIdentifierGenerator;

public abstract class IDPart {

	// Flags to identify whether series-based identifiers
	// wrap to their initial values, after the last value
	// in the series is reached.
	final boolean WRAP = true;
	final boolean NO_WRAP = false;
	
	// An identifier generator
	protected StringIdentifierGenerator generator;
	
	// Constructor
	public IDPart(StringIdentifierGenerator idGenerator) {
		setGenerator(idGenerator);
	}

	// Sets the identifier generator
	protected void setGenerator(StringIdentifierGenerator idGenerator) {
		if (idGenerator != null) {
			generator = idGenerator;
		}
	}

	// Gets the next identifier
	public String nextIdentifier() {
		// @TODO: Add Exception-handling here ...
		return generator.nextStringIdentifier();
	};

	// public boolean validate() {};
 
}
