/*	
 * IDService
 *
 * Interface for the methods of the ID Service.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Based on work by Richard Millet and Sanjay Dalal.
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */
 
package org.collectionspace.services;

import org.collectionspace.services.id.*;

public interface IDService {

	// public final static String ID_SCHEMA_NAME = "id"; // Note to self: Where is this used?

	// ----------------------------------------
	// IDs
	// ----------------------------------------

	// Create

	// Read single object
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
