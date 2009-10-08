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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.collectionspace.services.id.test;

import java.util.List;
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;

import org.collectionspace.services.id.*;

import org.testng.Assert;
import org.testng.annotations.Test;

/**        
 * IDServiceJdbcImplTest
 *
 * Unit tests for the ID Service's JDBC implementation class, IDServiceJdbcImpl.
 *
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */
public class IDServiceJdbcImplTest {

    String csid;
    String nextId;
    String serializedGenerator;
    SettableIDGenerator generator;
    
    IDServiceJdbcImpl jdbc = new IDServiceJdbcImpl();
    IDService service = jdbc;
    
    final static String TABLE_NAME = "id_generators";
    final static String DEFAULT_CSID = "TEST-1";
    
    final static String CURRENT_YEAR = YearIDGeneratorPart.getCurrentYear();


    @Test
    public void hasRequiredDatabaseTable() {
        Assert.assertTrue(
            jdbc.hasTable(TABLE_NAME), 
            "Table '" + TABLE_NAME + "' must exist in database. " + 
            "Please first run SQL setup script, " +
            "'create_id_generators_table.sql', in 'id' project.");
    }

    @Test(dependsOnMethods = {"hasRequiredDatabaseTable"})
    public void createIDGenerator() throws DocumentNotFoundException,
        IllegalArgumentException, IllegalStateException {
        try {
            jdbc.deleteIDGenerator(DEFAULT_CSID);
        } catch (Exception e) {
        	// Fail silently; this is guard code.
        }
        jdbc.createIDGenerator(DEFAULT_CSID, getSpectrumEntryNumberGenerator());
    }


    @Test(dependsOnMethods = {"hasRequiredDatabaseTable", "createIDGenerator"})
    public void readIDGenerator() throws DocumentNotFoundException,
        IllegalArgumentException, IllegalStateException {

        serializedGenerator = jdbc.readIDGenerator(DEFAULT_CSID);
        generator = IDGeneratorSerializer.deserialize(serializedGenerator);
        Assert.assertEquals(generator.getCsid(), DEFAULT_CSID);
    }


    @Test(dependsOnMethods =
        {"hasRequiredDatabaseTable", "createIDGenerator", "readIDGenerator"})
    public void readIDGeneratorsList() throws IllegalStateException {

        List generators = jdbc.readIDGeneratorsList();

        // @TODO Replace this placeholder test, which just
        // verifies that no error occurred while retrieving the list,
        // with a more meaningful test.
        Assert.assertTrue(generators.size() > 0);
    }
    @Test(dependsOnMethods =
        {"hasRequiredDatabaseTable", "createIDGenerator", "readIDGenerator",
          "readIDGeneratorsList"})
    public void readIDGeneratorsSummaryList() throws IllegalStateException {

        List generators = jdbc.readIDGeneratorsList();

        // @TODO Replace this placeholder test, which just
        // verifies that no error occurred while retrieving the list,
        // with a more meaningful test.
        Assert.assertTrue(generators.size() > 0);
    }

    @Test(dependsOnMethods = {"hasRequiredDatabaseTable", "createIDGenerator",
        "readIDGenerator"})
    public void updateIDGenerator() throws DocumentNotFoundException,
        BadRequestException, IllegalArgumentException, IllegalStateException {

        final String NEW_DESCRIPTION = "new description";
        
        // Retrieve an existing generator, deserialize it,
        // update its contents, serialize it, and write it back.
        serializedGenerator = jdbc.readIDGenerator(DEFAULT_CSID);
        generator = IDGeneratorSerializer.deserialize(serializedGenerator);
        generator.setDescription(NEW_DESCRIPTION);
        serializedGenerator = IDGeneratorSerializer.serialize(generator);

        jdbc.updateIDGenerator(DEFAULT_CSID, serializedGenerator);

        serializedGenerator = jdbc.readIDGenerator(DEFAULT_CSID);
        generator = IDGeneratorSerializer.deserialize(serializedGenerator);
        
        Assert.assertEquals(generator.getDescription(), NEW_DESCRIPTION);
        
    }

    @Test(dependsOnMethods = {"hasRequiredDatabaseTable", "createIDGenerator",
    	"readIDGenerator", "readIDGeneratorsList",
        "readIDGeneratorsSummaryList", "updateIDGenerator"})
    public void deleteIDGenerator() throws DocumentNotFoundException {
        jdbc.deleteIDGenerator(DEFAULT_CSID);
    }

    @Test(dependsOnMethods = {"hasRequiredDatabaseTable", "createIDGenerator",
        "readIDGenerator", "updateIDGenerator", "deleteIDGenerator"})
        public void createID() throws DocumentNotFoundException,
            BadRequestException, IllegalArgumentException,
            IllegalStateException {
                
        try {
            jdbc.deleteIDGenerator(DEFAULT_CSID);
        } catch (Exception e) {
        	// This deletion attempt may properly fail silently
        	// if no ID generator with the specified CSID currently
        	// exists in the database. 
        }

        jdbc.createIDGenerator(DEFAULT_CSID, getSpectrumEntryNumberGenerator());

        Assert.assertEquals(service.createID(DEFAULT_CSID), "E1");
        Assert.assertEquals(service.createID(DEFAULT_CSID), "E2");
        Assert.assertEquals(service.createID(DEFAULT_CSID), "E3");
        
        try {
            jdbc.deleteIDGenerator(DEFAULT_CSID);
        } catch (Exception e) {
            Assert.fail("Could not delete ID generator '" + DEFAULT_CSID + "'.");
        }
        
        jdbc.createIDGenerator(DEFAULT_CSID, getChinAccessionNumberGenerator());

        Assert.assertEquals(service.createID(DEFAULT_CSID), CURRENT_YEAR + ".1.1");
        Assert.assertEquals(service.createID(DEFAULT_CSID), CURRENT_YEAR + ".1.2");
        Assert.assertEquals(service.createID(DEFAULT_CSID), CURRENT_YEAR + ".1.3");

        try {
            jdbc.deleteIDGenerator(DEFAULT_CSID);
        } catch (Exception e) {
            Assert.fail("Could not delete ID generator '" + DEFAULT_CSID + "'.");
        }

    }


    // This test requires that:
    // 1. The ID Service is running and accessible to this test; and
    // 2. There is no ID generator retrievable through that service
    //        with the identifier 'non-existent identifier'.
    @Test(dependsOnMethods = {"hasRequiredDatabaseTable", "createID"}, 
        expectedExceptions = DocumentNotFoundException.class)
    public void createIDNonExistentGenerator() throws DocumentNotFoundException,
        BadRequestException, IllegalArgumentException, IllegalStateException {
        nextId = service.createID("non-existent identifier");                
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    // @TODO Read test patterns from external configuration.
    
    public String getSpectrumEntryNumberGenerator() {
        
        generator = new SettableIDGenerator(DEFAULT_CSID);
        generator.setDescription(
            "SPECTRUM entry number generator");
        generator.setURI(
            "urn:collectionspace:idpattern:spectrum-entry-number");
        generator.add(new StringIDGeneratorPart("E"));
        generator.add(new NumericIDGeneratorPart("1"));
        
        return IDGeneratorSerializer.serialize(generator);
        
    }

    public String getChinAccessionNumberGenerator() {

        generator = new SettableIDGenerator(DEFAULT_CSID);
        generator.setDescription(
            "CHIN accession number generator, for items without parts");
        generator.setURI(
            "urn:collectionspace:idpattern:chin-accession-number-no-parts");
        generator.add(new YearIDGeneratorPart());
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));
        generator.add(new StringIDGeneratorPart("."));
        generator.add(new NumericIDGeneratorPart("1"));        

        return IDGeneratorSerializer.serialize(generator);
        
    }

}
