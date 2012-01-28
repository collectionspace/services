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
import org.collectionspace.services.client.IntakeClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.intake.ConditionCheckerOrAssessorList;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.intake.InsurerList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;

//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
//import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntakeAuthRefsTest, carries out tests against a
 * deployed and running Intake Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class IntakeAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = IntakeAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = IntakeClient.SERVICE_PATH_COMPONENT;//"intakes";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceId = null;
    private List<String> intakeIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null; 
    private String currentOwnerRefName = null;
    private String depositorRefName = null;
    private String conditionCheckerOrAssessorRefName = null;
    private String insurerRefName = null;
    private String valuerRefName = null;
    private final int NUM_AUTH_REFS_EXPECTED = 5;
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

	@Override
	protected String getServiceName() {
		throw new UnsupportedOperationException(); //FIXME: REM - See http://issues.collectionspace.org/browse/CSPACE-3498
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the person refs and entities
        createPersonRefs();
        
        // Submit the request to the service and store the response.
        IntakeClient intakeClient = new IntakeClient();
        PoxPayloadOut multipart = createIntakeInstance(
                "entryNumber-" + identifier,
                CURRENT_DATE_UTC,
                currentOwnerRefName,
                depositorRefName,
                conditionCheckerOrAssessorRefName,
                insurerRefName,
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
        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, testExpectedStatusCode);

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
        intakeIdsCreated.add(extractId(res));
    }
    
    protected void createPersonRefs(){
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
    	PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    			PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        personAuthCSID = extractId(res);
        
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
        
        String csid = createPerson("Olivier", "Owner", "olivierOwner", authRefName);
        currentOwnerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
        csid = createPerson("Debbie", "Depositor", "debbieDepositor", authRefName);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
        csid = createPerson("Andrew", "Assessor", "andrewAssessor", authRefName);
        conditionCheckerOrAssessorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
        csid = createPerson("Ingrid", "Insurer", "ingridInsurer", authRefName);
        insurerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
        
        csid = createPerson("Vince", "Valuer", "vinceValuer", authRefName);
        valuerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
    }
    
    protected String createPerson(String firstName, String surName, String shortId, String authRefName ) {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
        PoxPayloadOut multipart = 
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID, 
    				authRefName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(testRequestType, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }

    // Success outcomes
    @Test(dataProvider="testName",
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        IntakeClient intakeClient = new IntakeClient();
        ClientResponse<String> res = intakeClient.read(knownResourceId);
        try {
	        assertStatusCode(res, testName);
	        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	        IntakesCommon intake = (IntakesCommon) extractPart(input,
	        		intakeClient.getCommonPartName(), IntakesCommon.class);
	        Assert.assertNotNull(intake);
	        // Check a couple of fields
	        Assert.assertEquals(intake.getCurrentOwner(), currentOwnerRefName);
	        Assert.assertEquals(intake.getConditionCheckersOrAssessors().getConditionCheckerOrAssessor().get(0), conditionCheckerOrAssessorRefName);
	        Assert.assertEquals(intake.getInsurers().getInsurer().get(0), insurerRefName);
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        // Get the auth refs and check them
        ClientResponse<AuthorityRefList> res2 = intakeClient.getAuthorityRefs(knownResourceId);
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
        IntakeClient intakeClient = new IntakeClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : intakeIdsCreated) {
            intakeClient.delete(resourceId).releaseConnection();
        }
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete persons before PersonAuth
        for (String resourceId : personIdsCreated) {
            personAuthClient.deleteItem(personAuthCSID, resourceId).releaseConnection();
        }
        personAuthClient.delete(personAuthCSID).releaseConnection();
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

   private PoxPayloadOut createIntakeInstance(String entryNumber,
    		String entryDate,
				String currentOwner,
				String depositor,
				String conditionCheckerAssessor,
				String insurer,
				String Valuer ) {
        IntakesCommon intake = new IntakesCommon();
        intake.setEntryNumber(entryNumber);
        intake.setEntryDate(entryDate);
        intake.setCurrentOwner(currentOwner);
        intake.setDepositor(depositor);
        intake.setValuer(Valuer);

        ConditionCheckerOrAssessorList checkerOrAssessorList = new ConditionCheckerOrAssessorList();
        List<String> checkersOrAssessors = checkerOrAssessorList.getConditionCheckerOrAssessor();
        checkersOrAssessors.add(conditionCheckerAssessor);
        intake.setConditionCheckersOrAssessors(checkerOrAssessorList);

        InsurerList insurerList = new InsurerList();
        List<String> insurers = insurerList.getInsurer();
        insurers.add(insurer);
        intake.setInsurers(insurerList);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(new IntakeClient().getCommonPartName(), intake);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, intake common");
            logger.debug(objectAsXmlString(intake, IntakesCommon.class));
        }

        return multipart;
    }
}
