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

import org.testng.Assert;
import org.testng.annotations.Test;

/**	
 * IDServiceJdbcImplTest
 *
 * Unit tests for the ID Service's JDBC implementation class, IDServiceJdbcImpl.
 *
 * $LastChangedBy: aron $
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */
public class IDServiceJdbcImplTest {

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
  
  final static String TABLE_NAME = "id_generators";
	final static String DEFAULT_CSID = "TEST-1";

  @Test
  public void hasRequiredDatabaseTable() {
    Assert.assertTrue(
      jdbc.hasTable(TABLE_NAME), 
      "Table '" + TABLE_NAME + "' must exist in database. " + 
      "Please first run SQL setup script, 'create_id_generators_table.sql', in 'id' project.");
  }

  @Test(dependsOnMethods = {"hasRequiredDatabaseTable"})
  public void addIDGenerator() {
    jdbc.addIDGenerator(DEFAULT_CSID, getSpectrumEntryNumberGenerator());
  }

  @Test(dependsOnMethods = {"addIDGenerator"})
  public void readIDGenerator() {

    serializedGenerator = jdbc.getIDGenerator(DEFAULT_CSID);
    pattern = IDGeneratorSerializer.deserialize(serializedGenerator);
    Assert.assertEquals(DEFAULT_CSID, pattern.getCsid());
    
  }

  @Test(dependsOnMethods = {"addIDGenerator"})
  public void updateIDGenerator() {

    final String NEW_DESCRIPTION = "new description";
    
    // Retrieve an existing generator, deserialize it,
    // update its contents, serialize it, and write it back.
    serializedGenerator = jdbc.getIDGenerator(DEFAULT_CSID);
    pattern = IDGeneratorSerializer.deserialize(serializedGenerator);
    pattern.setDescription(NEW_DESCRIPTION);
    serializedGenerator = IDGeneratorSerializer.serialize(pattern);
    
    jdbc.updateIDGenerator(DEFAULT_CSID, serializedGenerator);
    
    serializedGenerator = jdbc.getIDGenerator(DEFAULT_CSID);
    pattern = IDGeneratorSerializer.deserialize(serializedGenerator);
    
    Assert.assertEquals(NEW_DESCRIPTION, pattern.getDescription());
    
  }

  @Test(dependsOnMethods = {"addIDGenerator", "readIDGenerator"})
  public void deleteIDGenerator() {
    jdbc.deleteIDGenerator(DEFAULT_CSID);
  }
 
  @Test(dependsOnMethods = {"addIDGenerator", "deleteIDGenerator"})
	public void newIDValidPattern() {
	
    csid = DEFAULT_CSID;
    
    try {
      jdbc.deleteIDGenerator(csid);
    } catch (Exception e) {
      // do nothing
    }
    
    jdbc.addIDGenerator(csid, getSpectrumEntryNumberGenerator());

    Assert.assertEquals("E1", service.newID("TEST-1"));
    Assert.assertEquals("E2", service.newID("TEST-1"));
    Assert.assertEquals("E3", service.newID("TEST-1"));
    
    try {
      jdbc.deleteIDGenerator(csid);
    } catch (Exception e) {
      // do nothing
    }
    
    jdbc.addIDGenerator(csid, getChinAccessionNumberGenerator());

    String currentYear = YearIDGenerator.getCurrentYear();
    Assert.assertEquals(currentYear + ".1.1", service.newID("TEST-1"));
    Assert.assertEquals(currentYear + ".1.2", service.newID("TEST-1"));
    Assert.assertEquals(currentYear + ".1.3", service.newID("TEST-1"));

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
  @Test(dependsOnMethods = {"hasRequiredDatabaseTable"}, 
    expectedExceptions = IllegalArgumentException.class)
	public void newIDInvalidPattern() {
    nextId = service.newID("non-existent identifier");		
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
    
    return IDGeneratorSerializer.serialize(pattern);
    
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

    return IDGeneratorSerializer.serialize(pattern);
    
  }

}
