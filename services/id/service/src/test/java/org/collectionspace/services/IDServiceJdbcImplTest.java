/*	
 * IDServiceJdbcImplTest
 *
 * Test class for the ID Service's JDBC implementation class, IDServiceJdbcImpl.
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
 * $LastChangedBy: aron $
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */

package org.collectionspace.services.test;

import org.collectionspace.services.IDService;
import org.collectionspace.services.IDServiceJdbcImpl;
import org.collectionspace.services.id.YearIDGenerator;

//import org.collectionspace.services.id.Id;
//import org.collectionspace.services.id.IdList;
//import org.collectionspace.services.id.IdPattern;
//import org.collectionspace.services.id.IdPatternList;

import junit.framework.TestCase;
import static org.junit.Assert.*;

public class IDServiceJdbcImplTest extends TestCase {

  String currentYear = YearIDGenerator.getCurrentYear();
  String nextId;
  IDService service;

  protected void setUp() {
  
  	nextId = "";
	  service = new IDServiceJdbcImpl();
	  
  }

	public void testNextIDValidPattern() {

    assertEquals("E1", service.nextID("1"));		
    assertEquals(currentYear + ".1.1", service.nextID("2"));
    
	}

	public void testNextIDNotValidPattern() {
	
		try {
      nextId = service.nextID("3");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}
	
}
