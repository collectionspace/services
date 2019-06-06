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
package org.collectionspace.services.client.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PottagClient;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.pottag.PottagsCommon;
import org.collectionspace.services.person.PersonTermGroup;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PottagAuthRefsTest, carries out Authority References tests against a
 * deployed and running Pottag (aka Pot Tag) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class PottagAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = PottagAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    // Instance variables specific to this test.
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private List<String> pottagIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
	private String taggedByAuthRef;

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getCommonList(Response response) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the person refs and entities
        createPersonRefs();

        // Create a new Loans In resource.
        //
        // One or more fields in this resource will be PersonAuthority
        // references, and will refer to Person resources by their refNames.
        PottagClient pottagClient = new PottagClient();
        PoxPayloadOut pottageInstance = createPottagInstance("familyName-" + identifier, this.taggedByAuthRef,
        		"commonName-" + identifier);
        Response response = pottagClient.create(pottageInstance);
        try {
        	assertStatusCode(response, testName);	
	        // Store the ID returned from the first resource created for additional tests below.
	        if (knownResourceId == null) {
	            knownResourceId = extractId(response);
	        }
	        
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        pottagIdsCreated.add(extractId(response));
        } finally {
        	response.close();
        }
    }
    
    protected void createPersonRefs() throws Exception {
        PersonClient personAuthClient = new PersonClient();
        // Create a temporary PersonAuthority resource, and its corresponding
        // refName by which it can be identified.
    	PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        Response res = personAuthClient.create(multipart);
        try {
	        int statusCode = res.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode), invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	        personAuthCSID = extractId(res);
        } finally {
        	res.close();
        }
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, personAuthClient);
        
        // Create temporary Person resource, and a corresponding refName from which it can be identified.
       	String csid = createPerson("Harry", "Potter", "harryPotter", authRefName);
        this.personIdsCreated.add(csid);
        this.taggedByAuthRef = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, personAuthClient);
    }
    
    protected String createPerson(String firstName, String surName, String shortId, String authRefName ) throws Exception {
        PersonClient personAuthClient = new PersonClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        List<PersonTermGroup> personTerms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        String termName = firstName + " " + surName;
        term.setTermDisplayName(termName);
        term.setTermName(termName);
        personTerms.add(term);
    	PoxPayloadOut multipart =
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID, 
    				authRefName, personInfo, personTerms, personAuthClient.getItemCommonPartName());
    	
        Response res = personAuthClient.createItem(personAuthCSID, multipart);
        try {
	        int statusCode = res.getStatus();
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	    	return extractId(res);
        } finally {
        	res.close();
        }
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        PottagClient pottagClient = new PottagClient();
        Response res = pottagClient.read(knownResourceId);
        try {
	        assertStatusCode(res, testName);
	        // Extract the common part from the response.
	        PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	        PottagsCommon pottagCommon = (PottagsCommon) extractPart(input, pottagClient.getCommonPartName(),
	        		PottagsCommon.class);
	        Assert.assertNotNull(pottagCommon);
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        
        // Get the auth refs and check them
        res = pottagClient.getAuthorityRefs(knownResourceId);
        AuthorityRefList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.readEntity(AuthorityRefList.class);
	        Assert.assertNotNull(list);
        } finally {
        	if (res != null) {
        		res.close();
            }
        }
        
        int expectedAuthRefs = personIdsCreated.size();
        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        if (logger.isDebugEnabled()) {
            logger.debug("Expected " + expectedAuthRefs + " authority references, found " + numAuthRefsFound);
        }

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if (iterateThroughList && logger.isDebugEnabled()) {
            int i = 0;
            for(AuthorityRefList.AuthorityRefItem item : items){
                logger.debug(testName + ": list-item[" + i + "] Field:" +
                		item.getSourceField() + "= " +
                        item.getAuthDisplayName() +
                        item.getItemDisplayName());
                logger.debug(testName + ": list-item[" + i + "] refName=" +
                        item.getRefName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
                i++;
            }
        }

        Assert.assertEquals(numAuthRefsFound, expectedAuthRefs,
            "Did not find all expected authority references! " + "Expected " + expectedAuthRefs + ", found " + numAuthRefsFound);
    }


    // ---------------------------------------------------------------
    // Cleanup of resources created during testing
    // ---------------------------------------------------------------

    /**
     * Deletes all resources created by tests, after all tests have been run.
     *
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     * @throws Exception 
     */
    @AfterClass(alwaysRun=true)
    public void cleanUp() throws Exception {
        String noTest = System.getProperty("noTestCleanup");
    	if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
    	}
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        
        //
        // Delete all the pottag records we created
    	PottagClient pottagClient = new PottagClient();
        for (String resourceId : pottagIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            pottagClient.delete(resourceId).close(); // alternative to pottagClient.delete(resourceId).releaseConnection();
        }
        
        //
        // Delete Person resource(s) (before PersonAuthority resources).     
        PersonClient personAuthClient = new PersonClient();
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
       		personAuthClient.deleteItem(personAuthCSID, resourceId).close();
        }
        if (personAuthCSID != null) {
	        personAuthClient.delete(personAuthCSID).close();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    public String getServiceName() {
        return PottagClient.SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return PottagClient.SERVICE_PATH_COMPONENT;
    }

    private PoxPayloadOut createPottagInstance(String familyName,
    		String taggedBy,
    		String commonName) throws Exception {
    	PottagsCommon pottagCommon = new PottagsCommon();
    	pottagCommon.setFamily(familyName);
    	pottagCommon.setTaggedBy(taggedBy);
    	pottagCommon.setCommonName(commonName);

    	PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
    	PayloadOutputPart commonPart =
    			multipart.addPart(new PottagClient().getCommonPartName(), pottagCommon);

    	if(logger.isDebugEnabled()){
    		logger.debug("to be created, pottag common");
    		logger.debug(objectAsXmlString(pottagCommon, PottagsCommon.class));
    	}

    	return multipart;
    }

    @Override
    protected Class<AbstractCommonList> getCommonListType() {
    	return AbstractCommonList.class;
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
