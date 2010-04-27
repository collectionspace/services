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
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.authorityref.AuthorityRefList.AuthorityRefItem;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrganizationsCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoaninAuthRefsTest, carries out Authority References tests against a
 * deployed and running Loanin (aka Loans In) Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class OrgAuthorityAuthRefsTest extends BaseServiceTest {

   private final Logger logger =
       LoggerFactory.getLogger(OrgAuthorityAuthRefsTest.class);

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = "orgauthorities";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceRefName = null;
    private String knownAuthResourceId = null;
    private String knownItemId = null;
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    private Map<String, String> allItemResourceIdsCreated =
        new HashMap<String, String>();
    private List<String> personIdsCreated = new ArrayList();
    private int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
    private int OK_STATUS = Response.Status.OK.getStatusCode();
    private String personAuthCSID = null;
    private String organizationContactPersonRefName = null;
    private final int NUM_AUTH_REFS_EXPECTED = 1;

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {

        testSetup(CREATED_STATUS, ServiceRequestType.CREATE,testName);

        // Create a new Organization Authority resource.
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        String identifier = createIdentifier();
        String displayName = "TestOrgAuth-" + identifier;
        boolean createWithDisplayName = false;
    	knownResourceRefName =
            OrgAuthorityClientUtils.createOrgAuthRefName(displayName, createWithDisplayName);
        MultipartOutput multipart =
            OrgAuthorityClientUtils.createOrgAuthorityInstance(
		displayName, knownResourceRefName, new OrgAuthorityClient().getCommonPartName());

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = orgAuthClient.create(multipart);
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
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        knownAuthResourceId = extractId(res);
        allResourceIdsCreated.add(knownAuthResourceId);

        // Create all the person refs and entities
        createPersonRefs();

        // Initialize values for a new Organization item, to be created within
        // the newly-created Organization Authority resource.
        //
        // One or more fields in the Organization item record will
        // contain references to Persons, via their refNames, as
        // per the initialization(s) below.
        Map<String, String> testOrgMap = new HashMap<String,String>();
        testOrgMap.put(OrganizationJAXBSchema.SHORT_NAME,
            "Test Organization-" + identifier);
        testOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "Test Organization Name");
        testOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "Anytown, USA");
        testOrgMap.put(OrganizationJAXBSchema.CONTACT_NAME, organizationContactPersonRefName);

        // Finishing creating the new Organization item, then
        // submit the request to the service and store the response.
        knownItemId = OrgAuthorityClientUtils.createItemInAuthority(
            knownAuthResourceId, knownResourceRefName, testOrgMap, orgAuthClient);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the IDs from every item created by tests,
        // so they can be deleted after tests have been run.
        allItemResourceIdsCreated.put(knownItemId, knownAuthResourceId);

    }
    
    protected void createPersonRefs(){

        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Create a temporary PersonAuthority resource, and its corresponding
        // refName by which it can be identified.
    	String authRefName = 
    	    PersonAuthorityClientUtils.createPersonAuthRefName(PERSON_AUTHORITY_NAME, false);
    	MultipartOutput multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    PERSON_AUTHORITY_NAME, authRefName, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, CREATED_STATUS);
        personAuthCSID = extractId(res);

        // Create a temporary Person resource, and its corresponding refName
        // by which it can be identified.
        organizationContactPersonRefName =
            PersonAuthorityClientUtils.createPersonRefName(authRefName, "Charlie Orgcontact", true);
        personIdsCreated.add(createPerson("Charlie", "Orgcontact", organizationContactPersonRefName));

    }
    
    protected String createPerson(String firstName, String surName, String refName ) {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
    	MultipartOutput multipart = 
    	    PersonAuthorityClientUtils.createPersonInstance(personAuthCSID,
    		refName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, CREATED_STATUS);
    	return extractId(res);
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {

        // Perform setup.
        testSetup(OK_STATUS, ServiceRequestType.READ,testName);

        // Submit the request to the service and store the response.
        OrgAuthorityClient orgAuthClient = new OrgAuthorityClient();
        ClientResponse<MultipartInput> res =
            orgAuthClient.readItem(knownAuthResourceId, knownItemId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ".read: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        MultipartInput input = (MultipartInput) res.getEntity();
        OrganizationsCommon organization = (OrganizationsCommon) extractPart(input,
            orgAuthClient.getItemCommonPartName(), OrganizationsCommon.class);
        Assert.assertNotNull(organization);
        if(logger.isDebugEnabled()){
            logger.debug(objectAsXmlString(organization, OrganizationsCommon.class));
        }
        // Check one or more of the authority fields in the Organization item
        Assert.assertEquals(organization.getContactName(), organizationContactPersonRefName);
        
        // Get the auth refs and check them
        // FIXME - need to create this method in the client
        // and get the ID for the organization item
        ClientResponse<AuthorityRefList> res2 =
           orgAuthClient.getItemAuthorityRefs(knownAuthResourceId, knownItemId);
        statusCode = res2.getStatus();

        if(logger.isDebugEnabled()){
            logger.debug(testName + ".getAuthorityRefs: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        AuthorityRefList list = res2.getEntity();

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){
            List<AuthorityRefList.AuthorityRefItem> items =
                list.getAuthorityRefItem();
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
            Assert.assertEquals(i, NUM_AUTH_REFS_EXPECTED, "Did not find all authrefs!");
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
        ClientResponse<Response> res;
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            res = personAuthClient.deleteItem(personAuthCSID, resourceId);
        }
        // Delete PersonAuthority resource(s).
        // Note: Any non-success response is ignored and not reported.
        res = personAuthClient.delete(personAuthCSID);
        String parentResourceId;
        String itemResourceId;
        OrgAuthorityClient client = new OrgAuthorityClient();
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allItemResourceIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            res = client.deleteItem(parentResourceId, itemResourceId);
        }
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            res = client.delete(resourceId);
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private MultipartOutput createOrgAuthorityInstance(String identifier) {
    	String displayName = "displayName-" + identifier;
    	String refName = OrgAuthorityClientUtils.createOrgAuthRefName(displayName, true);
        return OrgAuthorityClientUtils.createOrgAuthorityInstance(
				displayName, refName,
				new OrgAuthorityClient().getCommonPartName());
    }

}
