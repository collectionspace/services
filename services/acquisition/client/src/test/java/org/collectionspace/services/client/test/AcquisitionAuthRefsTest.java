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
import org.collectionspace.services.client.AcquisitionClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.acquisition.AcquisitionsCommon;
//import org.collectionspace.services.acquisition.AcquisitionsCommonList;

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
 * AcquisitionAuthRefsTest, carries out tests against a
 * deployed and running Acquisition Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class AcquisitionAuthRefsTest extends BaseServiceTest {

   private final Logger logger =
       LoggerFactory.getLogger(AcquisitionAuthRefsTest.class);

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = "acquisitions";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceId = null;
    private List<String> acquisitionIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
    private int OK_STATUS = Response.Status.OK.getStatusCode();
    private String personAuthCSID = null; 
    private String acquisitionAuthorizerRefName = null;
    private String acquisitionFundingSourceRefName = null;
    // Not ready for multiples, yet
    //private String acquisitionSourcesRefName = null;
    private String fieldCollectorRefName = null;
    private final int NUM_AUTH_REFS_EXPECTED = 3;

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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {

        testSetup(CREATED_STATUS, ServiceRequestType.CREATE,testName);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the person refs and entities
        createPersonRefs();
        
        MultipartOutput multipart = createAcquisitionInstance(
                				"April 1, 2010",
								acquisitionAuthorizerRefName,
								acquisitionFundingSourceRefName,
								fieldCollectorRefName );

        AcquisitionClient acquisitionClient = new AcquisitionClient();
        ClientResponse<Response> res = acquisitionClient.create(multipart);

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
        acquisitionIdsCreated.add(extractId(res));
    }
    
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
        Assert.assertEquals(statusCode, CREATED_STATUS);
        personAuthCSID = extractId(res);
        
        acquisitionAuthorizerRefName = PersonAuthorityClientUtils.createPersonRefName(
        							authRefName, "Annie Authorizer", true);
        personIdsCreated.add(createPerson("Annie", "Authorizer", acquisitionAuthorizerRefName));
        
        acquisitionFundingSourceRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Sammy Source", true);
        personIdsCreated.add(createPerson("Sammy", "Source", acquisitionFundingSourceRefName));
        
        
        fieldCollectorRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Connie Collector", true);
        personIdsCreated.add(createPerson("Connie", "Collector", fieldCollectorRefName));
    }
    
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
        AcquisitionClient acquisitionClient = new AcquisitionClient();
        ClientResponse<MultipartInput> res = acquisitionClient.read(knownResourceId);
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
        AcquisitionsCommon acquisition = (AcquisitionsCommon) extractPart(input,
        		acquisitionClient.getCommonPartName(), AcquisitionsCommon.class);
        Assert.assertNotNull(acquisition);
        // Check a couple of fields
        Assert.assertEquals(acquisition.getAcquisitionAuthorizer(), acquisitionAuthorizerRefName);
        Assert.assertEquals(acquisition.getFieldCollector(), fieldCollectorRefName);
        
        // Get the auth refs and check them
        ClientResponse<AuthorityRefList> res2 =
            acquisitionClient.getAuthorityRefs(knownResourceId);
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
                		item.getSourceField() + "= " +
                        item.getAuthDisplayName() +
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
        AcquisitionClient acquisitionClient = new AcquisitionClient();
        for (String resourceId : acquisitionIdsCreated) {
           // Note: Any non-success responses are ignored and not reported.
           ClientResponse<Response> res = acquisitionClient.delete(resourceId);
           res.releaseConnection();
        }
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete persons before PersonAuth
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            ClientResponse<Response> res = personAuthClient.deleteItem(personAuthCSID, resourceId);
            res.releaseConnection();
        }
        // Note: Any non-success response is ignored and not reported.
        ClientResponse<Response> res = personAuthClient.delete(personAuthCSID);
        res.releaseConnection();
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

   private MultipartOutput createAcquisitionInstance(
    			String accessionDate,
				String acquisitionAuthorizer,
				String acquisitionFundingSource,
				String fieldCollector ) {
        AcquisitionsCommon acquisition = new AcquisitionsCommon();
        acquisition.setAccessionDate(accessionDate);
        acquisition.setAcquisitionAuthorizer(acquisitionAuthorizer);
        acquisition.setAcquisitionFundingSource(acquisitionFundingSource);
        acquisition.setFieldCollector(fieldCollector);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(acquisition, MediaType.APPLICATION_XML_TYPE);
        AcquisitionClient acquisitionClient = new AcquisitionClient();
        commonPart.getHeaders().add("label", acquisitionClient.getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, acquisition common");
            logger.debug(objectAsXmlString(acquisition, AcquisitionsCommon.class));
        }

        return multipart;
    }
}
