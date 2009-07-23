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

package org.collectionspace.services.test.id;

// import org.collectionspace.services.IDService;
// import org.collectionspace.services.IDServiceJdbcImpl;
// import org.collectionspace.services.id.*;

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

  final static String DEFAULT_SERIALIZED_ID_PATTERN =
    "<org.collectionspace.services.id.IDPattern>\n" +
    "  <csid>" + DEFAULT_CSID + "</csid>\n" +
    "  <uri></uri>\n" +
    "  <description></description>\n" +
    "  <parts/>\n" +
    "</org.collectionspace.services.id.IDPattern>";

  // @TODO We may want to canonicalize (or otherwise normalize) the expected and
  // actual XML in these tests, to avoid failures resulting from differences in
  // whitespace, etc.
	public void testSerializeIDPattern() {
	  IDPattern pattern = new IDPattern(DEFAULT_CSID);
		assertEquals(DEFAULT_SERIALIZED_ID_PATTERN, jdbc.serializeIDPattern(pattern));
	}

	public void testSerializeNullIDPattern() {
	  try {
	    String serializedPattern = jdbc.serializeIDPattern(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testDeserializeIDPattern() {
	  // This test will fail with different hash codes unless we add an IDPattern.equals()
	  // method that explicitly defines object equality as, for instance, having identical values
	  // in each of its instance variables.
	  // IDPattern pattern = jdbc.deserializeIDPattern(DEFAULT_SERIALIZED_ID_PATTERN);
	  // assertEquals(pattern, new IDPattern(DEFAULT_CSID));
	}

	public void testDeserializeNullSerializedIDPattern() {
	  try {
	    IDPattern pattern = jdbc.deserializeIDPattern(null);
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

	public void testDeserializeInvalidSerializedIDPattern() {
	  try {
	    IDPattern pattern = jdbc.deserializeIDPattern("<invalid_serialized_pattern/>");
			fail("Should have thrown IllegalArgumentException here");
		} catch (IllegalArgumentException expected) {
			// This Exception should be thrown, and thus the test should pass.
		}
	}

  // @TODO Read test patterns from external configuration.
  public String generateSpectrumEntryNumberTestPattern() {
    
    pattern = new IDPattern(DEFAULT_CSID);
    pattern.setDescription("SPECTRUM entry number pattern");
    pattern.setURI("urn:collectionspace:idpattern:spectrum-entry-number");
    pattern.add(new StringIDPart("E"));
    pattern.add(new NumericIDPart("1"));
    
    return jdbc.serializeIDPattern(pattern);
    
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

    return jdbc.serializeIDPattern(pattern);
    
  }
  
  public void testAddIDPattern() {

    jdbc.addIDPattern(DEFAULT_CSID, generateSpectrumEntryNumberTestPattern());
    
  }

  public void testReadIDPattern() {

    serializedPattern = jdbc.getIDPattern(DEFAULT_CSID);
    pattern = jdbc.deserializeIDPattern(serializedPattern);
    assertEquals(DEFAULT_CSID, pattern.getCsid());
    
  }

  public void testUpdateIDPattern() {

    final String NEW_DESCRIPTION = "new description";
    
    serializedPattern = jdbc.getIDPattern(DEFAULT_CSID);
    
    pattern = jdbc.deserializeIDPattern(serializedPattern);
    pattern.setDescription(NEW_DESCRIPTION);
    serializedPattern = jdbc.serializeIDPattern(pattern);
    
    jdbc.updateIDPattern(DEFAULT_CSID, serializedPattern);
    
    serializedPattern = jdbc.getIDPattern(DEFAULT_CSID);
    pattern = jdbc.deserializeIDPattern(serializedPattern);
    
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

	
}
