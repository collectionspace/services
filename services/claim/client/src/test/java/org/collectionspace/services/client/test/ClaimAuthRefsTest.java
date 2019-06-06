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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.ClaimClient;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.claim.ClaimsCommon;
import org.collectionspace.services.claim.ClaimantGroupList;
import org.collectionspace.services.claim.ClaimantGroup;
import org.collectionspace.services.person.PersonTermGroup;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClaimAuthRefsTest, carries out Authority References tests against a
 * deployed and running Claim Service.
 *
 * $LastChangedRevision: 4159 $
 * $LastChangedDate: 2011-02-15 16:11:08 -0800 (Tue, 15 Feb 2011) $
 */
public class ClaimAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = ClaimAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceId = null;
    private List<String> claimIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String claimFilerRefName = null;
    private String claimOnBehalfOfRefName = null;

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
	protected AbstractCommonList getCommonList(
			Response response) {
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

        // Create a new Claim resource.
        //
        // One or more fields in this resource will be PersonAuthority
        // references, and will refer to Person resources by their refNames.
        ClaimClient claimClient = new ClaimClient();
        PoxPayloadOut claminInstance = createClaimInstance("claimNumber-" + identifier,
                claimFilerRefName,
                claimOnBehalfOfRefName);
        Response res = claimClient.create(claminInstance);
        try {
	        int statusCode = res.getStatus();	
	        if (logger.isDebugEnabled()) {
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode), invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        String csid = extractId(res);
	        if (knownResourceId == null) {
	            knownResourceId = csid;
	        }
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        claimIdsCreated.add(csid);
        } finally {
        	res.close();
        }
    }

    protected void createPersonRefs() throws Exception{
        // Create a temporary PersonAuthority resource, and its corresponding
        // refName by which it can be identified.
        PersonClient personAuthClient = new PersonClient();
    	PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        Response res = personAuthClient.create(multipart);
        try {
	        int statusCode = res.getStatus();
	
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	            invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	        personAuthCSID = extractId(res);
        } finally {
        	res.close();
        }
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, personAuthClient);
        
        // Create temporary Person resources, and their corresponding refNames
        // by which they can be identified.
       	String csid = createPerson("Carrie", "ClaimFiler", "carrieClaimFiler", authRefName);
        personIdsCreated.add(csid);
        claimFilerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, personAuthClient);

       	csid = createPerson("Ben", "BehalfOf", "benBehalfOf", authRefName);
        personIdsCreated.add(csid);
        claimOnBehalfOfRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, personAuthClient);
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
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        ClaimClient claimClient = new ClaimClient();
        Response res = claimClient.read(knownResourceId);
        try {
	        assertStatusCode(res, testName);	
	        PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	        ClaimsCommon claimCommon = (ClaimsCommon) extractPart(input, claimClient.getCommonPartName(), ClaimsCommon.class);
	        Assert.assertNotNull(claimCommon);
	        
	        if(logger.isDebugEnabled()){
	            logger.debug(objectAsXmlString(claimCommon, ClaimsCommon.class));
	        }
	        // Check a couple of fields
	        Assert.assertEquals(claimCommon.getClaimantGroupList().getClaimantGroup().get(0).getClaimFiledBy(), claimFilerRefName);
	        Assert.assertEquals(claimCommon.getClaimantGroupList().getClaimantGroup().get(0).getClaimFiledOnBehalfOf(), claimOnBehalfOfRefName);
        } finally {
        	if (res != null) {
                res.close();
            }
        }
        
        // Get the auth refs and check them
        res = claimClient.getAuthorityRefs(knownResourceId);
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
            for (AuthorityRefList.AuthorityRefItem item : items) {
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
    	// Delete Claim resource(s).
        ClaimClient claimClient = new ClaimClient();
        for (String resourceId : claimIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            claimClient.delete(resourceId).close();
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

    @Override
    protected String getServiceName() {
        return ClaimClient.SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return ClaimClient.SERVICE_PATH_COMPONENT;
    }

   private PoxPayloadOut createClaimInstance(String claimNumber,
            String claimFiler,
            String claimFiledOnBehalfOf) throws Exception {
        ClaimsCommon claimCommon = new ClaimsCommon();
        claimCommon.setClaimNumber(claimNumber);
        ClaimantGroupList claimantGroupList = new ClaimantGroupList();
        ClaimantGroup claimantGroup = new ClaimantGroup();
        claimantGroup.setClaimFiledBy(claimFiler);
        claimantGroup.setClaimFiledOnBehalfOf(claimFiledOnBehalfOf);
        claimantGroupList.getClaimantGroup().add(claimantGroup);
        claimCommon.setClaimantGroupList(claimantGroupList);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(claimCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new ClaimClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, claim common");
            logger.debug(objectAsXmlString(claimCommon, ClaimsCommon.class));
        }

        return multipart;
    }

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
		throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
	}
}
