/*	
 * IDService
 *
 * Interface for the methods of the ID Service.
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
 * Based on work by Richard Millet and Sanjay Dalal.
 *
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */
 
package org.collectionspace.services;

public interface IDService {

	// ----------------------------------------
	// IDs
	// ----------------------------------------

	// Create

	// Read single object
	//
	// Returns the next available ID associated with a specified ID pattern.
	public String nextID(String csid) throws IllegalArgumentException, IllegalStateException;
	
	// Read a list of objects (aka read multiple)
	
	// ----------------------------------------
	// ID Patterns
	// ----------------------------------------
	
	// Create
	
	// Read single object
	
	// Read a list of objects (aka read multiple)
	
	// Update
	
	// Delete (possibly not permitted - deactivate instead?)

}
