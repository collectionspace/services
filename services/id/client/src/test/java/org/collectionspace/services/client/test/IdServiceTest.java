/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
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
package org.collectionspace.services.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IdClient;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.id.IDGeneratorSerializer;
import org.collectionspace.services.id.NumericIDGeneratorPart;
import org.collectionspace.services.id.SettableIDGenerator;
import org.collectionspace.services.id.StringIDGeneratorPart;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IdServiceTest, carries out tests against a
 * deployed and running ID Service.
 *
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class IdServiceTest extends BaseServiceTest {

    /** The logger. */
    private final String CLASS_NAME = IdServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    String knownResourceId = "";
    private List<String> allResourceIdsCreated = new ArrayList<String>();

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return (CollectionSpaceClient) new IdClient();
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    // Success outcomes
    
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        IdClient client = new IdClient();
        
        String xmlPayload = getSampleSerializedIdGenerator();
        logger.debug("payload=\n" + xmlPayload);
        ClientResponse<Response> res = client.create(xmlPayload);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
        
        String newID = extractId(res);
        
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId.isEmpty()){
        	knownResourceId = newID;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);

    }


    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create"})
    public void createId(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        IdClient client = new IdClient();
        ClientResponse<String> res = client.createId(knownResourceId);
        String generatedId = null;
        try {
	        assertStatusCode(res, testName);
	        generatedId = res.getEntity();
	        Assert.assertNotNull(generatedId);
	        Assert.assertFalse(generatedId.isEmpty());
	        if (logger.isDebugEnabled()) {
	            logger.debug("generated ID=" + generatedId);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        // Create a second ID.  Verify that it is different from the first.
        // Assumes that the last part in the ID pattern generates values
        // that will always differ at each generation.
        res = client.createId(knownResourceId);
        try {
	        assertStatusCode(res, testName);
	        String secondGeneratedId = res.getEntity();
	        Assert.assertNotNull(secondGeneratedId);
	        Assert.assertFalse(secondGeneratedId.isEmpty());
	        Assert.assertFalse(secondGeneratedId.equals(generatedId));
	        if (logger.isDebugEnabled()) {
	            logger.debug("second generated ID=" + secondGeneratedId);
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
    }

    // Failure outcomes
    // None at present.

    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------

    // Success outcomes

    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);
        
        if(logger.isDebugEnabled()){
            logger.debug("Reading ID Generator at CSID " + knownResourceId + " ...");
        }

        // Submit the request to the service and store the response.
        IdClient client = new IdClient();
        ClientResponse<String> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        String entity = res.getEntity();
        Assert.assertNotNull(entity);
        if (logger.isDebugEnabled()) {
            logger.debug("entity body=\r" + entity);
        }
    }

    // Failure outcomes
    // None at present.

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes


    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create"})
    public void readList(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ_LIST);

        // Submit the request to the service and store the response.
        IdClient client = new IdClient();
        ClientResponse<String> res = client.readList();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

        String entity = res.getEntity();
        Assert.assertNotNull(entity);
        if (logger.isDebugEnabled()) {
            logger.debug("entity body=\r" + entity);
        }

    }

    // Failure outcomes
    // None at present.
    
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "createId", "read", "readList"})
    public void delete(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.DELETE);

        // Submit the request to the service and store the response.
        IdClient client = new IdClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return new IdClient().getServicePathComponent();
    }

    @Override
    protected String getServiceName() { 
    	throw new UnsupportedOperationException(); //FIXME: REM - See http://issues.collectionspace.org/browse/CSPACE-3498 }
    }
    
    /**
     * Returns a serialized ID generator, based on the SPECTRUM entry number pattern,
     * as a sample ID generator to be used in tests.
     * 
     * This is a minimal ID Generator, containing only ID Generator Parts,
     * and lacking a display name, description etc.
     * 
     * @return a serialized ID Generator
     * @throws BadRequestException 
     */
    public String getSampleSerializedIdGenerator() throws BadRequestException {
        
        SettableIDGenerator generator = new SettableIDGenerator();
        generator.add(new StringIDGeneratorPart("E"));
        generator.add(new NumericIDGeneratorPart("1"));       
        return IDGeneratorSerializer.serialize(generator);

    }
}

