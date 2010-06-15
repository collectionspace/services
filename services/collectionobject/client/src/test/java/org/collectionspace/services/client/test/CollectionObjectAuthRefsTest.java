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
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectAuthRefsTest, carries out tests against a
 * deployed and running CollectionObject Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class CollectionObjectAuthRefsTest extends BaseServiceTest {

   /** The logger. */
    private final String CLASS_NAME = CollectionObjectAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    /** The service path component. */
    final String SERVICE_PATH_COMPONENT = "collectionobjects";
    
    /** The person authority name. */
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    
    /** The known resource id. */
    private String knownResourceId = null;
    
    /** The collection object ids created. */
    private List<String> collectionObjectIdsCreated = new ArrayList<String>();
    
    /** The person ids created. */
    private List<String> personIdsCreated = new ArrayList<String>();
    
    /** The person auth csid. */
    private String personAuthCSID = null; 
    
    /** The content organization ref name. */
    private String contentOrganizationRefName = null;
    
    /** The content people ref name. */
    private String contentPeopleRefName = null;
    
    /** The content person ref name. */
    private String contentPersonRefName = null;
    
    /** The inscriber ref name. */
    private String contentInscriberRefName = null;
    
    /** The number of authority references expected. */
    private final int NUM_AUTH_REFS_EXPECTED = 4;

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
	protected AbstractCommonList getAbstractCommonList(
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the person refs and entities
        createPersonRefs();
        
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        MultipartOutput multipart = createCollectionObjectInstance(
        						"Obj Title",
        						"ObjNum-1234",
								contentOrganizationRefName,
								contentPeopleRefName,
								contentPersonRefName,
								contentInscriberRefName );

        ClientResponse<Response> res = collectionObjectClient.create(multipart);

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

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        collectionObjectIdsCreated.add(extractId(res));
    }
    
    /**
     * Creates the person refs.
     */
    protected void createPersonRefs(){
    	String authRefName = 
    		PersonAuthorityClientUtils.createPersonAuthRefName(PERSON_AUTHORITY_NAME, false);
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
    	MultipartOutput multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    			PERSON_AUTHORITY_NAME, authRefName, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        personAuthCSID = extractId(res);
        
        contentOrganizationRefName =
            PersonAuthorityClientUtils.createPersonRefName(authRefName, "Omni Org", true);
        personIdsCreated.add(createPerson("Omni", "Org", contentOrganizationRefName));
        
        contentPeopleRefName =
            PersonAuthorityClientUtils.createPersonRefName(authRefName, "Pushy People", true);
        personIdsCreated.add(createPerson("Pushy", "People", contentPeopleRefName));
        
        contentPersonRefName =
                PersonAuthorityClientUtils.createPersonRefName(authRefName, "Connie ContactPerson", true);
        personIdsCreated.add(createPerson("Connie", "ContactPerson", contentPersonRefName));
        
        contentInscriberRefName =
            PersonAuthorityClientUtils.createPersonRefName(authRefName, "Ingrid Inscriber", true);
        personIdsCreated.add(createPerson("Ingrid", "Inscriber", contentInscriberRefName));
    }
    
    /**
     * Creates the person.
     *
     * @param firstName the first name
     * @param surName the sur name
     * @param refName the ref name
     * @return the string
     */
    protected String createPerson(String firstName, String surName, String refName ) {
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
    	MultipartOutput multipart = 
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID, 
    				refName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }

    // Success outcomes
    /**
     * Read and check auth refs.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        ClientResponse<MultipartInput> res = collectionObjectClient.read(knownResourceId);
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
        CollectionobjectsCommon collectionObject = (CollectionobjectsCommon) extractPart(input,
        		collectionObjectClient.getCommonPartName(), CollectionobjectsCommon.class);
        Assert.assertNotNull(collectionObject);
        // Check a couple of fields
        Assert.assertEquals(collectionObject.getContentOrganization(), contentOrganizationRefName);
        Assert.assertEquals(collectionObject.getInscriptionContentInscriber(), contentInscriberRefName);
        
        // Get the auth refs and check them
        ClientResponse<AuthorityRefList> res2 = collectionObjectClient.getAuthorityRefs(knownResourceId);
        statusCode = res2.getStatus();

        if(logger.isDebugEnabled()){
            logger.debug(testName + ".getAuthorityRefs: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        AuthorityRefList list = res2.getEntity();
        
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
        if(iterateThroughList && logger.isDebugEnabled()){;
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
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        for (String resourceId : collectionObjectIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            collectionObjectClient.delete(resourceId).releaseConnection();
        }
        // Note: Any non-success response is ignored and not reported.
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete persons before PersonAuth
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId).releaseConnection();
        }
        personAuthClient.delete(personAuthCSID).releaseConnection();
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

   /**
    * Creates the collection object instance.
    *
    * @param title the title
    * @param objNum the obj num
    * @param contentOrganization the content organization
    * @param contentPeople the content people
    * @param contentPerson the content person
    * @param inscriber the inscriber
    * @return the multipart output
    */
   private MultipartOutput createCollectionObjectInstance(
		   		String title,
		   		String objNum,
				String contentOrganization,
				String contentPeople,
				String contentPerson,
				String inscriber ) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
        collectionObject.setTitle(title);
        collectionObject.setObjectNumber(objNum);
        collectionObject.setContentOrganization(contentOrganization);
        collectionObject.setContentPeople(contentPeople);
        collectionObject.setContentPerson(contentPerson);
        collectionObject.setInscriptionContentInscriber(inscriber);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(collectionObject, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", new CollectionObjectClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, collectionObject common");
            logger.debug(objectAsXmlString(collectionObject, CollectionobjectsCommon.class));
        }

        return multipart;
    }
}
