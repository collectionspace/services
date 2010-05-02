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
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.intake.IntakesCommonList;
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
 * PersonAuthRefDocsTest, carries out tests against a
 * deployed and running Person Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class PersonAuthRefDocsTest extends BaseServiceTest {

   private final Logger logger =
       LoggerFactory.getLogger(PersonAuthRefDocsTest.class);

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = "intakes";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownIntakeId = null;
    private List<String> intakeIdsCreated = new ArrayList();
    private List<String> personIdsCreated = new ArrayList();
    private int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
    private int OK_STATUS = Response.Status.OK.getStatusCode();
    private String personAuthCSID = null; 
    private String currentOwnerPersonCSID = null; 
    private String currentOwnerRefName = null;
    private String depositorRefName = null;
    private String conditionCheckAssesorRefName = null;
    private String insurerRefName = null;
    private String fieldCollectorRefName = null;
    private String valuerRefName = null;
    private final int NUM_AUTH_REF_DOCS_EXPECTED = 1;

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
    public void createIntakeWithAuthRefs(String testName) throws Exception {

        testSetup(CREATED_STATUS, ServiceRequestType.CREATE,testName);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the person refs and entities
        createPersonRefs();

        IntakeClient intakeClient = new IntakeClient();
        MultipartOutput multipart = createIntakeInstance(
                "entryNumber-" + identifier,
                "entryDate-" + identifier,
								currentOwnerRefName,
								depositorRefName,
								conditionCheckAssesorRefName,
								insurerRefName,
								fieldCollectorRefName,
								valuerRefName );

        ClientResponse<Response> res = intakeClient.create(multipart);

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
        if (knownIntakeId == null){
            knownIntakeId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownIntakeId=" + knownIntakeId);
            }
        }
        
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        intakeIdsCreated.add(extractId(res));
    }
    
    protected void createPersonRefs(){
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
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
        
        currentOwnerRefName = PersonAuthorityClientUtils.createPersonRefName(
        							authRefName, "Olivier Owner", true);
				currentOwnerPersonCSID = createPerson("Olivier", "Owner", currentOwnerRefName);
        personIdsCreated.add(currentOwnerPersonCSID);
        
        depositorRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Debbie Depositor", true);
        personIdsCreated.add(createPerson("Debbie", "Depositor", depositorRefName));
        
        conditionCheckAssesorRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Andrew Assessor", true);
        personIdsCreated.add(createPerson("Andrew", "Assessor", conditionCheckAssesorRefName));
        
        insurerRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Ingrid Insurer", true);
        personIdsCreated.add(createPerson("Ingrid", "Insurer", insurerRefName));
        
        fieldCollectorRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Connie Collector", true);
        personIdsCreated.add(createPerson("Connie", "Collector", fieldCollectorRefName));
        
        valuerRefName = PersonAuthorityClientUtils.createPersonRefName(
									authRefName, "Vince Valuer", true);
        personIdsCreated.add(createPerson("Vince", "Valuer", valuerRefName));
        

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
        dependsOnMethods = {"createIntakeWithAuthRefs"})
    public void readAndCheckAuthRefDocs(String testName) throws Exception {

        // Perform setup.
        testSetup(OK_STATUS, ServiceRequestType.READ,testName);
        
        // Get the auth ref docs and check them
       PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
       ClientResponse<AuthorityRefDocList> refDocListResp =
        	personAuthClient.getReferencingObjects(personAuthCSID, currentOwnerPersonCSID);

        int statusCode = refDocListResp.getStatus();

        if(logger.isDebugEnabled()){
            logger.debug(testName + ".getReferencingObjects: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        AuthorityRefDocList list = refDocListResp.getEntity();

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        boolean fFoundIntake = false;
        if(iterateThroughList && logger.isDebugEnabled()){
            List<AuthorityRefDocList.AuthorityRefDocItem> items =
                    list.getAuthorityRefDocItem();
            int i = 0;
            logger.debug(testName + ": Docs that use: " + currentOwnerRefName);
            for(AuthorityRefDocList.AuthorityRefDocItem item : items){
                logger.debug(testName + ": list-item[" + i + "] " +
                		item.getDocType() + "(" +
                		item.getDocId() + ") Name:[" +
                		item.getDocName() + "] Number:[" +
                		item.getDocNumber() + "] in field:[" +
                		item.getSourceField() + "]");
                if(!fFoundIntake && knownIntakeId.equalsIgnoreCase(item.getDocId())) {
               		fFoundIntake = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundIntake, "Did not find Intake with authref!");
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
        IntakeClient intakeClient = new IntakeClient();
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : intakeIdsCreated) {
            ClientResponse<Response> res = intakeClient.delete(resourceId);
        }
        // Delete persons before PersonAuth
        for (String resourceId : personIdsCreated) {
            ClientResponse<Response> res = personAuthClient.deleteItem(personAuthCSID, resourceId);
        }
        ClientResponse<Response> res = personAuthClient.delete(personAuthCSID);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

   private MultipartOutput createIntakeInstance(String entryNumber,
    		String entryDate,
				String currentOwner,
				String depositor,
				String conditionCheckAssesor,
				String insurer,
				String fieldCollector,
				String Valuer ) {
        IntakesCommon intake = new IntakesCommon();
        intake.setEntryNumber(entryNumber);
        intake.setEntryDate(entryDate);
        intake.setCurrentOwner(currentOwner);
        intake.setDepositor(depositor);
        intake.setConditionCheckAssesor(conditionCheckAssesor);
        intake.setInsurer(insurer);
        intake.setFieldCollector(fieldCollector);
        intake.setValuer(Valuer);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(intake, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", new IntakeClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, intake common");
            logger.debug(objectAsXmlString(intake, IntakesCommon.class));
        }

        return multipart;
    }
}
