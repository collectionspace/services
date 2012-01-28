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

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.organization.MainBodyGroup;
import org.collectionspace.services.organization.MainBodyGroupList;
import org.collectionspace.services.organization.OrganizationsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoaninAuthRefsTest, carries out Authority References tests against a
 * deployed and running Loanin (aka Loans In) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class OrgAuthorityAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

   /** The logger. */
    private final String CLASS_NAME = OrgAuthorityAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    final String ORG_AUTHORITY_NAME = "TestOrgAuth";
    
	@Override
	public String getServicePathComponent() {
		return OrgAuthorityClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return OrgAuthorityClient.SERVICE_NAME;
	}

    protected String knownItemResourceId = null;
	
    private String knownResourceRefName = null;
            
    /** The person ids created. */
    private List<String> personIdsCreated = new ArrayList<String>();
    
    // CSID for the instance of the test Person authority
    // created during testing.
    private String personAuthCSID = null;
    
    /** The organization contact person refNames. */
    private String organizationContactPersonRefName1 = null;
    private String organizationContactPersonRefName2 = null;

    // The refName of an Organization item that represents
    // the sub-body organization of a second Organization item.
    private String subBodyRefName = null;
    
    /** The number of authorityreferences expected. */
    private final int NUM_AUTH_REFS_EXPECTED = 2;	// Place authRef not legal, should not be returned.

    protected void setKnownResource( String id, String refName ) {
    	knownResourceId = id;
    	knownResourceRefName = refName;
    }

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
			ClientResponse<AbstractCommonList> response) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /**
     * Creates the with auth refs.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName")
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Create a new Organization Authority resource.
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        String shortId = createIdentifier();
        String displayName = "TestOrgAuth-" + shortId;
    	//String baseRefName = OrgAuthorityClientUtils.createOrgAuthRefName(shortId, null);
        PoxPayloadOut multipart =
            OrgAuthorityClientUtils.createOrgAuthorityInstance(
            			displayName, shortId, orgAuthClient.getCommonPartName());

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = orgAuthClient.create(multipart);
        try {
	        int statusCode = res.getStatus();
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        //
	        // Specifically:
	        // Does it fall within the set of valid status codes?
	        // Does it exactly match the expected status code?
	        if(logger.isDebugEnabled()){
	            logger.debug(testName + ": status = " + statusCode);
	        }
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	            invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, testExpectedStatusCode);
	
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        String newId = extractId(res);
	        if (knownResourceId == null){
	        	setKnownResource( newId, null ); //baseRefName );
	        }
	        allResourceIdsCreated.add(newId);
        } finally {
            res.releaseConnection();
        }        

        // Create all the person refs and entities
        createPersonRefs();

        // Create all the organization sub-body refs and entities
        createSubBodyOrgRefs();

        // Initialize values for a new Organization item, to be created within
        // the newly-created Organization Authority resource.
        //
        // One or more fields in the Organization item record will
        // contain references to Persons, via their refNames, as
        // per the initialization(s) below.
        Map<String, String> testOrgMap = new HashMap<String,String>();
        testOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId);
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "Anytown, USA");

        Map<String, List<String>> testOrgRepeatablesMap = new HashMap<String,List<String>>();
        List<String> testOrgContactNames = new ArrayList<String>();
        testOrgContactNames.add(organizationContactPersonRefName1);
        testOrgContactNames.add(organizationContactPersonRefName2);
        testOrgRepeatablesMap.put(OrganizationJAXBSchema.CONTACT_NAMES, testOrgContactNames);
        List<String> testOrgSubBodies = new ArrayList<String>();
        testOrgSubBodies.add(subBodyRefName);
        testOrgRepeatablesMap.put(OrganizationJAXBSchema.SUB_BODIES, testOrgSubBodies);

        MainBodyGroupList mainBodyList = new MainBodyGroupList();
        List<MainBodyGroup> mainBodyGroups = mainBodyList.getMainBodyGroup();
        MainBodyGroup mainBodyGroup = new MainBodyGroup();
        mainBodyGroup.setShortName("Test Organization-" + shortId);
        mainBodyGroup.setLongName("Test Organization Name");
        mainBodyGroups.add(mainBodyGroup);

        // Finishing creating the new Organization item, then
        // submit the request to the service and store the response.
        knownItemResourceId = OrgAuthorityClientUtils.createItemInAuthority(
        		knownResourceId, knownResourceRefName, testOrgMap,
                        testOrgRepeatablesMap, mainBodyList, orgAuthClient);

        // Store the IDs from every item created by tests,
        // so they can be deleted after tests have been run.
        allResourceItemIdsCreated.put(knownItemResourceId, knownResourceId);
    }
    
    /**
     * Creates the person refs.
     */
    protected void createPersonRefs() {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Create a temporary PersonAuthority resource, and its corresponding
        // refName by which it can be identified.
    	PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        
    	ClientResponse<Response> res = personAuthClient.create(multipart);
        try {
            int statusCode = res.getStatus();
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, STATUS_CREATED);
            personAuthCSID = extractId(res);
        } finally {
            res.releaseConnection();
        }

        //String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
        
        // Create temporary Person resources, and their corresponding refNames
        // by which they can be identified.
       	String csid = createPerson("Charlie", "Orgcontact", "charlieOrgcontact", null ); // authRefName);
        personIdsCreated.add(csid);
        organizationContactPersonRefName1 = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        // Create temporary Person resources, and their corresponding refNames
        // by which they can be identified.
       	csid = createPerson("Chelsie", "Contact", "chelsieContact", null ); // authRefName);
        personIdsCreated.add(csid);
        organizationContactPersonRefName2 = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
    }
    
    /**
     * Creates the person.
     *
     * @param firstName the first name
     * @param surName the sur name
     * @param shortId
     * @param authRefName
     * @return the string
     */
    protected String createPerson(String firstName, String surName, String shortId, String authRefName ) {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
    	PoxPayloadOut multipart = 
    	    PersonAuthorityClientUtils.createPersonInstance(personAuthCSID,
    	    		authRefName, personInfo, personAuthClient.getItemCommonPartName());
        
    	String result = null;
    	ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
    	try {
	        int statusCode = res.getStatus();
	
	        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
	                invalidStatusCodeMessage(testRequestType, statusCode));
	        Assert.assertEquals(statusCode, STATUS_CREATED);
	    	result = extractId(res);
    	} finally {
    		res.releaseConnection();
    	}
    	
    	return result;
    }

    private void createSubBodyOrgRefs() {
        // Create a temporary sub-body Organization resource, and its corresponding refName
        // by which it can be identified.
        //
        // This sub-body Organization resource will be created in the same
        // Organization authority as its parent Organization resource.

        String subBodyResourceId = createSubBodyOrganization("Test SubBody Organization");
        allResourceItemIdsCreated.put(subBodyResourceId, knownResourceId);
        subBodyRefName = OrgAuthorityClientUtils.getOrgRefName(knownResourceId, subBodyResourceId, null);
    }

    protected String createSubBodyOrganization(String subBodyName) {
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        Map<String, String> subBodyOrgMap = new HashMap<String,String>();
        String shortId = createIdentifier();
        subBodyOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortId );
        subBodyOrgMap.put(OrganizationJAXBSchema.SHORT_NAME,
            subBodyName + "-" + shortId);
        subBodyOrgMap.put(OrganizationJAXBSchema.LONG_NAME, subBodyName + " Long Name");
        subBodyOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, subBodyName + " Founding Place");
    	PoxPayloadOut multipart =
    	    OrgAuthorityClientUtils.createOrganizationInstance(
    		knownResourceRefName, subBodyOrgMap, orgAuthClient.getItemCommonPartName());

    	String result = null;
    	ClientResponse<Response> res = orgAuthClient.createItem(knownResourceId, multipart);
    	try {
            int statusCode = res.getStatus();
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, STATUS_CREATED);
            result = extractId(res);
    	} finally {
    	    res.releaseConnection();
    	}

    	return result;
    }

    // Success outcomes
    /**
     * Read and check auth refs.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName",
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        ClientResponse<String> res = orgAuthClient.readItem(knownResourceId, knownItemResourceId);
        OrganizationsCommon organization = null;
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        organization = (OrganizationsCommon) extractPart(input,
	            orgAuthClient.getItemCommonPartName(), OrganizationsCommon.class);
	        Assert.assertNotNull(organization);
	        if (logger.isDebugEnabled()){
	            logger.debug(objectAsXmlString(organization, OrganizationsCommon.class));
	        }
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        // Check one or more of the authority fields in the Organization item
        Assert.assertEquals(organization.getContactNames().getContactName().get(0),
                organizationContactPersonRefName1);
        Assert.assertEquals(organization.getContactNames().getContactName().get(1),
                organizationContactPersonRefName2);
        Assert.assertEquals(organization.getSubBodies().getSubBody().get(0),
                subBodyRefName);

        // Get the auth refs and check them
        // FIXME - need to create this method in the client
        // and get the ID for the organization item
        ClientResponse<AuthorityRefList> res2 =
           orgAuthClient.getItemAuthorityRefs(knownResourceId, knownItemResourceId);
        AuthorityRefList list = null;
        try {
	        assertStatusCode(res2, testName);
	        list = res2.getEntity();
        } finally {
        	if (res2 != null) {
        		res2.releaseConnection();
            }
        }
        
        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        if(logger.isDebugEnabled()){
            logger.debug("Expected " + NUM_AUTH_REFS_EXPECTED +
                " authority references, found " + numAuthRefsFound);
        }
        Assert.assertEquals(numAuthRefsFound, NUM_AUTH_REFS_EXPECTED,
            "Did not find all expected authority references! " +
            "Expected " + NUM_AUTH_REFS_EXPECTED + ", found " + numAuthRefsFound);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){
            int i = 0;
            for(AuthorityRefList.AuthorityRefItem item : items){
                logger.debug(testName + ": list-item[" + i + "] Field:" +
                    item.getSourceField() + "=" +
                    item.getItemDisplayName());
                logger.debug(testName + ": list-item[" + i + "] refName=" +
                    item.getRefName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                    item.getUri());
                i++;
            }
        }
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
     */
    @AfterClass(alwaysRun=true)
    public void cleanUp() {
        String noTest = System.getProperty("noTestCleanup");
    	if(Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
    	}
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete Person resource(s) (before PersonAuthority resources).
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId).releaseConnection();
        }
        // Delete PersonAuthority resource(s).
        // Note: Any non-success response is ignored and not reported.
        if(personAuthCSID!=null) {
        	personAuthClient.delete(personAuthCSID).releaseConnection();
        }
        
        String parentResourceId;
        String itemResourceId;
        OrgAuthorityClient client = new OrgAuthorityClient();
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            client.deleteItem(parentResourceId, itemResourceId).releaseConnection();
        }
        
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            client.delete(resourceId).releaseConnection();
        }
    }
}
