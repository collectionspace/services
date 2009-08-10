/**
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
 */
 
package org.collectionspace.services.id;

/**
 * IDService
 *
 * Interface for the ID Service.
 *
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public interface IDService {

	// ----------------------------------------
	// IDs
	// ----------------------------------------

	// Create

	// Read single object
	
    // Generates and returns a new ID from the specified ID generator.
    public String newID(String csid)
      throws IllegalArgumentException, IllegalStateException;
    
    // Returns the last-generated ID associated with the specified ID generator.
    public String getLastID(String csid)
      throws IllegalArgumentException, IllegalStateException;

	// Read a list of objects (aka read multiple)
	
	// ----------------------------------------
	// ID Patterns
	// ----------------------------------------
	
	// @TODO Change this to IDGenerators in the next refactoring.
	
	// Create
	
	  // Adds a new ID pattern.
    public void addIDPattern(String csid, String serializedIDPattern)
      throws IllegalArgumentException, IllegalStateException;
	
	  // Read single object
    public String getIDPattern(String csid)
      throws IllegalArgumentException, IllegalStateException;
	
	// Read a list of objects (aka read multiple)
	
	  // Update (may need to check for changes in the ID pattern structure)
    public void updateIDPattern(String csid, String serializedIDPattern)
      throws IllegalArgumentException, IllegalStateException;
	
	// Delete (possibly not permitted - deactivate instead?)

}
