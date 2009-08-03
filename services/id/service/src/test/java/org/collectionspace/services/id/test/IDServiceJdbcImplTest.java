/**	
 * IDServiceJdbcImplTest
 *
 * Unit tests for the ID Service's JDBC implementation class, IDServiceJdbcImpl.
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

package org.collectionspace.services.id.test;

import org.collectionspace.services.id.*;

import junit.framework.TestCase;
import static org.junit.Assert.*;

public class IDServiceJdbcImplTest extends TestCase {

  String csid;
  String nextId;
  String serializedPattern;
  IDPattern pattern;
  
  IDServiceJdbcImpl jdbc = new IDServiceJdbcImpl();
  IDService service = jdbc;

	final static String DEFAULT_CSID = "TEST-1";
  
  public void testPlaceholder() {
    // Placeholder test to avoid "org.testng.TestNGException:
    // Failure in JUnit mode ...: could not create/run JUnit test suite"
    // errors until we add working tests to this class.
  }
 
/*

  public void testAddIDPattern() {
    jdbc.addIDPattern(DEFAULT_CSID, generateSpectrumEntryNumberTestPattern());
  }

  public void testReadIDPattern() {

    serializedPattern = jdbc.getIDPattern(DEFAULT_CSID);
    pattern = IDPatternSerializer.deserialize(serializedPattern);
    assertEquals(DEFAULT_CSID, pattern.getCsid());
    
  }

  public void testUpdateIDPattern() {

    final String NEW_DESCRIPTION = "new description";
    
    serializedPattern = jdbc.getIDPattern(DEFAULT_CSID);
    
    pattern = IDPatternSerializer.deserialize(serializedPattern);
    pattern.setDescription(NEW_DESCRIPTION);
    serializedPattern = IDPatternSerializer.serialize(pattern);
    
    jdbc.updateIDPattern(DEFAULT_CSID, serializedPattern);
    
    serializedPattern = jdbc.getIDPattern(DEFAULT_CSID);
    pattern = IDPatternSerializer.deserialize(serializedPattern);
    
    assertEquals(NEW_DESCRIPTION, pattern.getDescription());
    
  }

  public void testDeleteIDPattern() {
    jdbc.deleteIDPattern(DEFAULT_CSID);
  }
 
	public void testNextIDValidPattern() {
	
    csid = DEFAULT_CSID;
    
    try {
      jdbc.deleteIDPattern(csid);
    } catch (Exception e) {
      // do nothing
    }
    
    jdbc.addIDPattern(csid, generateSpectrumEntryNumberTestPattern());

    assertEquals("E1", service.nextID("TEST-1"));
    assertEquals("E2", service.nextID("TEST-1"));
    assertEquals("E3", service.nextID("TEST-1"));
    
    try {
      jdbc.deleteIDPattern(csid);
    } catch (Exception e) {
      // do nothing
    }
    
    jdbc.addIDPattern(csid, generateChinAccessionNumberTestPattern());

    String currentYear = YearIDGenerator.getCurrentYear();
    assertEquals(currentYear + ".1.1", service.nextID("TEST-1"));
    assertEquals(currentYear + ".1.2", service.nextID("TEST-1"));
    assertEquals(currentYear + ".1.3", service.nextID("TEST-1"));

    try {
      jdbc.deleteIDPattern(csid);
    } catch (Exception e) {
      // do nothing
    }
    
	}

  // This test requires that:
  // 1. The ID Service is running and accessible to this test; and
  // 2. There is no ID pattern retrievable through that service
  //    with the identifier 'non-existent identifier'.
	public void testNextIDInvalidPattern() {
	
		try {
      nextId = service.nextID("non-existent identifier");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}
	
*/

  // ---------------------------------------------------------------
  // Utility methods used by tests above
  // ---------------------------------------------------------------

  // @TODO Read test patterns from external configuration.
  public String generateSpectrumEntryNumberTestPattern() {
    
    pattern = new IDPattern(DEFAULT_CSID);
    pattern.setDescription("SPECTRUM entry number pattern");
    pattern.setURI("urn:collectionspace:idpattern:spectrum-entry-number");
    pattern.add(new StringIDPart("E"));
    pattern.add(new NumericIDPart("1"));
    
    return IDPatternSerializer.serialize(pattern);
    
  }

  // @TODO Read test patterns from external configuration.
  public String generateChinAccessionNumberTestPattern() {

    pattern = new IDPattern(DEFAULT_CSID);
    pattern.setDescription("CHIN accession number pattern, for items without parts");
    pattern.setURI("urn:collectionspace:idpattern:chin-accession-number-no-parts");
    pattern.add(new YearIDPart());
    pattern.add(new StringIDPart("."));
    pattern.add(new NumericIDPart("1"));
    pattern.add(new StringIDPart("."));
    pattern.add(new NumericIDPart("1"));    

    return IDPatternSerializer.serialize(pattern);
    
  }
	
}
