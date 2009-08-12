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

package org.collectionspace.services.id.test;

import org.collectionspace.services.id.*;

import junit.framework.TestCase;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**	
 * IDServiceJdbcImplTest
 *
 * Unit tests for the ID Service's JDBC implementation class, IDServiceJdbcImpl.
 *
 * $LastChangedBy: aron $
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */
public class IDServiceJdbcImplTest extends TestCase {

  // *IMPORTANT*
  // @TODO This class is in an early state of a refactoring to
  // reflect a change from IDPatterns to IDGenerators at the top level
  // of the ID Service.  As a result, there will be some naming
  // inconsistencies throughout this source file.

  String csid;
  String nextId;
  String serializedGenerator;
  IDPattern pattern;
  
  IDServiceJdbcImpl jdbc = new IDServiceJdbcImpl();
  IDService service = jdbc;

	final static String DEFAULT_CSID = "TEST-1";

  @BeforeClass
  public static void setUpOnce() { 
    // @TODO Check for service preconditions before running tests.   
  }
  
  @Test
  public void testPlaceholder() {
    // Placeholder test to avoid "org.testng.TestNGException:
    // Failure in JUnit mode ...: could not create/run JUnit test suite"
    // errors until we add working tests to this class.
  }

  @Test
  public void testAddIDGenerator() {
    jdbc.addIDGenerator(DEFAULT_CSID, getSpectrumEntryNumberGenerator());
  }

  @Test
  public void testReadIDGenerator() {

    serializedGenerator = jdbc.getIDGenerator(DEFAULT_CSID);
    pattern = IDPatternSerializer.deserialize(serializedGenerator);
    assertEquals(DEFAULT_CSID, pattern.getCsid());
    
  }

  @Test
  public void testUpdateIDGenerator() {

    final String NEW_DESCRIPTION = "new description";
    
    // Retrieve an existing generator, deserialize it,
    // update its contents, serialize it, and write it back.
    serializedGenerator = jdbc.getIDGenerator(DEFAULT_CSID);
    pattern = IDPatternSerializer.deserialize(serializedGenerator);
    pattern.setDescription(NEW_DESCRIPTION);
    serializedGenerator = IDPatternSerializer.serialize(pattern);
    
    jdbc.updateIDGenerator(DEFAULT_CSID, serializedGenerator);
    
    serializedGenerator = jdbc.getIDGenerator(DEFAULT_CSID);
    pattern = IDPatternSerializer.deserialize(serializedGenerator);
    
    assertEquals(NEW_DESCRIPTION, pattern.getDescription());
    
  }

  @Test
  public void testDeleteIDGenerator() {
    jdbc.deleteIDGenerator(DEFAULT_CSID);
  }
 
  @Test
	public void testNewIDValidPattern() {
	
    csid = DEFAULT_CSID;
    
    try {
      jdbc.deleteIDGenerator(csid);
    } catch (Exception e) {
      // do nothing
    }
    
    jdbc.addIDGenerator(csid, getSpectrumEntryNumberGenerator());

    assertEquals("E1", service.newID("TEST-1"));
    assertEquals("E2", service.newID("TEST-1"));
    assertEquals("E3", service.newID("TEST-1"));
    
    try {
      jdbc.deleteIDGenerator(csid);
    } catch (Exception e) {
      // do nothing
    }
    
    jdbc.addIDGenerator(csid, getChinAccessionNumberGenerator());

    String currentYear = YearIDGenerator.getCurrentYear();
    assertEquals(currentYear + ".1.1", service.newID("TEST-1"));
    assertEquals(currentYear + ".1.2", service.newID("TEST-1"));
    assertEquals(currentYear + ".1.3", service.newID("TEST-1"));

    try {
      jdbc.deleteIDGenerator(csid);
    } catch (Exception e) {
      // do nothing
    }
    
	}

  // This test requires that:
  // 1. The ID Service is running and accessible to this test; and
  // 2. There is no ID pattern retrievable through that service
  //    with the identifier 'non-existent identifier'.
  @Test
	public void testNextIDInvalidPattern() {
	
		try {
      nextId = service.newID("non-existent identifier");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
		
	}

  // ---------------------------------------------------------------
  // Utility methods used by tests above
  // ---------------------------------------------------------------

  // @TODO Read test patterns from external configuration.
  
  public String getSpectrumEntryNumberGenerator() {
    
    pattern = new IDPattern(DEFAULT_CSID);
    pattern.setDescription("SPECTRUM entry number pattern");
    pattern.setURI("urn:collectionspace:idpattern:spectrum-entry-number");
    pattern.add(new StringIDPart("E"));
    pattern.add(new NumericIDPart("1"));
    
    return IDPatternSerializer.serialize(pattern);
    
  }

  public String getChinAccessionNumberGenerator() {

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
