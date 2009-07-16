/*	
 * IDServiceJdbcImpl
 *
 * Implementation of portions of the ID Service that use JDBC
 * for persistence of IDPatterns, IDParts, and ID values.
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

// @TODO: Replace wildcarded import statement for
// org.collectionspace.services.id.* with class-specific
// import statements.  Determine how to properly handle
// what may be a varying set, over time, of subclasses of IDPart.

// @TODO: Retrieve IDPatterns from the database (via JDBC or
// Hibernate) at initialization and refresh time.

// @TODO: Handle concurrency.
//
// Right now, with each new request we're simply instantiating
// a new IDPattern and returning its next ID.  As a result,
// the generated IDs may well duplicate other, previously-generated IDs.
//
// When we start storing IDPatterns and IDs in a database, the
// the current ID associated with each pattern will be stored
// and modified in a single location.
//
// At that point, we'll also need to add code to ensure that only
// one IDPattern object is instantiated for each stored pattern,
// at initialization or reset time, as well as to handle concurrent
// requests.

// @TODO: Verify access (public, protected, or private) to service methods.

package org.collectionspace.services;

import org.collectionspace.services.IDService;
// The following import statement has been left open-ended
// to accommodate future ID generation components.
import org.collectionspace.services.id.*;

public class IDServiceJdbcImpl implements IDService {

  // Placeholder constructor.
  public void IDServiceJdbcImpl() {
  }

  // Returns the next ID associated with an IDPattern.
  //
  // Sets the current ID of that IDPattern to the just-generated ID.
	//
	// TODO: Implement logic to check for ID availability, after generating
	// a candidate ID.
	public String nextID(String csid) throws
		IllegalArgumentException, IllegalStateException {
		
		IDPattern pattern;
		String nextId = "";

		if (csid == null || csid.equals("")) {
			throw new IllegalArgumentException(
				"Identifier for ID pattern must not be null or empty.");
		}

		try {
			pattern = getIDPattern(csid);
		} catch (IllegalArgumentException e ) {
			throw e;
		}
		
		// Guard code - should not be needed.
		if (pattern == null) {
			throw new IllegalArgumentException(
				"Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
		}
		
		// Get the next ID associated with the pattern,
		// setting its current ID to the just-generated ID.
		//
		// @TODO: On the first request, return the initial ID
		// for this pattern, rather than the next ID.
		try {
			nextId = pattern.nextID();
		} catch (IllegalStateException e ) {
			throw e;
		}
		
		return nextId;

	}
	
	// Returns an IDPattern.
	public IDPattern getIDPattern(String csid) throws IllegalArgumentException {
	
	  IDPattern pattern;

    // @TODO: Replace hard-coded IDs and related logic - currently
    // used here for bootstrapping and initial testing - with actual
    // IDPattern identifiers - CSIDs, and URIs or whatever other
    // identifier type we may be using alongside CSIDs - and logic
    // that dynamically retrieves previously-stored patterns.
    
    // @TODO: Retrieve IDPatterns from the database using JDBC,
    // rather than hard-coding their construction here.
   		
   	// Pattern that returns SPECTRUM Entry numbers
   	// (e.g. "Ennnn").
    if (csid.equals("1")) {
    	
    	// Retrieve the pattern.  (In this example, we're
    	// simply hard-coding its construction here.)
  		pattern = new IDPattern();
			pattern.add(new StringIDPart("E"));
			pattern.add(new NumericIDPart("0"));
			
			return pattern;
    	
		// Pattern that returns SPECTRUM Accession numbers, with item numbers
		// (e.g. "YYYY.nnnn.nnnn").
    } else if (csid.equals("2")) {

    	// Retrieve the pattern.(In this example, we're
    	// simply hard-coding its construction here.)
  		pattern = new IDPattern();
			pattern.add(new YearIDPart());
			pattern.add(new StringIDPart("."));
			pattern.add(new NumericIDPart("1"));
			pattern.add(new StringIDPart("."));
			pattern.add(new NumericIDPart("0"));
			
			return pattern;

		} else {
			throw new IllegalArgumentException(
				"Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
		}
		
	}
		
}
