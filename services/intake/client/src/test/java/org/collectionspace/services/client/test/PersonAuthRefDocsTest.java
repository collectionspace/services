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
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
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
 * PersonAuthRefDocsTest, carries out tests against a
 * deployed and running Person Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class PersonAuthRefDocsTest extends BaseServiceTest<AbstractCommonList> {

    private final String CLASS_NAME = PersonAuthRefDocsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = "intakes";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownIntakeId = null;
    private List<String> intakeIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String personShortId = PERSON_AUTHORITY_NAME;
    private String currentOwnerPersonCSID = null;
    private String depositorPersonCSID = null;
    private String insurerPersonCSID = null;
    private String currentOwnerRefName = null;
    private String depositorRefName = null;
    private String conditionCheckerAssessorRefName = null;
    private String insurerRefName = null;
    private String valuerRefName = null;
    private String valuerShortId = null;
    private final int NUM_AUTH_REF_DOCS_EXPECTED = 1;
    private final static String CURRENT_DATE_UTC =
            GregorianCalendarDateTimeUtils.currentDateUTC();

    @Override
    public String getServiceName() { 
    	throw new UnsupportedOperationException(); //FIXME: REM - http://issues.collectionspace.org/browse/CSPACE-3498   
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
    @Test(dataProvider = "testName")
    public void createIntakeWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        // Create all the person refs and entities
        createPersonRefs();

        IntakeClient intakeClient = new IntakeClient();
        PoxPayloadOut multipart = createIntakeInstance(
                "entryNumber-" + identifier,
                CURRENT_DATE_UTC,
                currentOwnerRefName,
                depositorRefName,
                conditionCheckerAssessorRefName,
                insurerRefName,
                valuerRefName);

        ClientResponse<Response> res = intakeClient.create(multipart);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            //
            // Specifically:
            // Does it fall within the set of valid status codes?
            // Does it exactly match the expected status code?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
        } finally {
            res.releaseConnection();
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownIntakeId == null) {
            knownIntakeId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownIntakeId=" + knownIntakeId);
            }
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        intakeIdsCreated.add(extractId(res));
    }

    /**
     * Creates the person refs.
     */
    protected void createPersonRefs() {
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
        Assert.assertNotNull(csid);
        currentOwnerPersonCSID = csid;
        currentOwnerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        Assert.assertNotNull(currentOwnerRefName);
        personIdsCreated.add(csid);

        csid = createPerson("Debbie", "Depositor", "debbieDepositor", authRefName);
        Assert.assertNotNull(csid);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        depositorPersonCSID = csid;
        Assert.assertNotNull(depositorRefName);
        personIdsCreated.add(csid);

        csid = createPerson("Andrew", "Assessor", "andrewAssessor", authRefName);
        Assert.assertNotNull(csid);
        conditionCheckerAssessorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        Assert.assertNotNull(conditionCheckerAssessorRefName);
        personIdsCreated.add(csid);

        csid = createPerson("Ingrid", "Insurer", "ingridInsurer", authRefName);
        Assert.assertNotNull(csid);
        insurerPersonCSID = csid;
        insurerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        Assert.assertNotNull(insurerRefName);
        personIdsCreated.add(csid);

        csid = createPerson("Vince", "Valuer", "vinceValuer", authRefName);
        Assert.assertNotNull(csid);
        valuerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        if (logger.isDebugEnabled()) {
            logger.debug("valuerShortId=" + valuerShortId);
        }
        Assert.assertNotNull(valuerRefName);
        personIdsCreated.add(csid);

    }

    protected String createPerson(String firstName, String surName, String shortId, String authRefName) {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String, String>();
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
    @Test(dataProvider = "testName", dependsOnMethods = {"createIntakeWithAuthRefs"})
    public void readAndCheckAuthRefDocs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Get the auth ref docs and check them

        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        ClientResponse<AuthorityRefDocList> res =
                personAuthClient.getReferencingObjects(personAuthCSID, currentOwnerPersonCSID);
        AuthorityRefDocList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        List<AuthorityRefDocList.AuthorityRefDocItem> items =
                list.getAuthorityRefDocItem();
        Assert.assertTrue(items != null);
        Assert.assertTrue(items.size() > 0);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        boolean fFoundIntake = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            int i = 0;
            logger.debug(testName + ": Docs that use: " + currentOwnerRefName);
            for (AuthorityRefDocList.AuthorityRefDocItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] "
                        + item.getDocType() + "("
                        + item.getDocId() + ") Name:["
                        + item.getDocName() + "] Number:["
                        + item.getDocNumber() + "] in field:["
                        + item.getSourceField() + "]");
                if (!fFoundIntake && knownIntakeId.equalsIgnoreCase(item.getDocId())) {
                    fFoundIntake = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundIntake, "Did not find Intake with authref!");
        }
        //
        // Get the referencing objects
        //
        personAuthClient = new PersonAuthorityClient();
        res = personAuthClient.getReferencingObjects(personAuthCSID, depositorPersonCSID);
        try {
	        assertStatusCode(res, testName);
	        list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        items = list.getAuthorityRefDocItem();
        Assert.assertTrue(items != null);
        Assert.assertTrue(items.size() > 0);
        Assert.assertTrue(items.get(0) != null);
        
        // Optionally output additional data about list members for debugging.
        iterateThroughList = true;
        fFoundIntake = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            int i = 0;
            logger.debug(testName + ": Docs that use: " + depositorRefName);
            for (AuthorityRefDocList.AuthorityRefDocItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] "
                        + item.getDocType() + "("
                        + item.getDocId() + ") Name:["
                        + item.getDocName() + "] Number:["
                        + item.getDocNumber() + "] in field:["
                        + item.getSourceField() + "]");
                if (!fFoundIntake && knownIntakeId.equalsIgnoreCase(item.getDocId())) {
                    fFoundIntake = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundIntake, "Did not find Intake with authref!");
        }
    }

    /*
     * Read and check the list of referencing objects, where the authRef field
     * is a value instance of a repeatable scalar field.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createIntakeWithAuthRefs"},
    		groups = {"repeatableScalar"})
    public void readAndCheckAuthRefDocsRepeatableScalar(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Get the auth ref docs and check them

        // Single scalar field
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        ClientResponse<AuthorityRefDocList> res =
                personAuthClient.getReferencingObjects(personAuthCSID, insurerPersonCSID);
        AuthorityRefDocList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.getEntity();
        } finally {
        	if (res != null) {
                res.releaseConnection();
            }
        }
        
        List<AuthorityRefDocList.AuthorityRefDocItem> items =
                list.getAuthorityRefDocItem();
        Assert.assertTrue(items != null);
        Assert.assertTrue(items.size() > 0);
        Assert.assertTrue(items.get(0) != null);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        boolean fFoundIntake = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            int i = 0;
            logger.debug(testName + ": Docs that use: " + insurerRefName);
            for (AuthorityRefDocList.AuthorityRefDocItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] "
                        + item.getDocType() + "("
                        + item.getDocId() + ") Name:["
                        + item.getDocName() + "] Number:["
                        + item.getDocNumber() + "] in field:["
                        + item.getSourceField() + "]");
                if (!fFoundIntake && knownIntakeId.equalsIgnoreCase(item.getDocId())) {
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
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
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
        IntakeClient intakeClient = new IntakeClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : intakeIdsCreated) {
            ClientResponse<Response> res = intakeClient.delete(resourceId);
            res.releaseConnection();
        }
        // Delete persons before PersonAuth
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        for (String resourceId : personIdsCreated) {
            ClientResponse<Response> res = personAuthClient.deleteItem(personAuthCSID, resourceId);
            res.releaseConnection();
        }
        if (personAuthCSID != null) {
            personAuthClient.delete(personAuthCSID).releaseConnection();
        }
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
            String Valuer) {
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

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, intake common");
            logger.debug(objectAsXmlString(intake, IntakesCommon.class));
        }

        return multipart;
    }
}
