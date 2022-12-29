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
import org.collectionspace.services.client.HitClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.hit.HitsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.person.PersonTermGroup;
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
    final String SERVICE_PATH_COMPONENT = "hits";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownHitId = null;
    private List<String> hitIdsCreated = new ArrayList<String>();
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

	@Override
	protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) {
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
    @Test(dataProvider = "testName")
    public void createHitWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        // Create all the person refs and entities
        createPersonRefs();

        HitClient hitClient = new HitClient();
        PoxPayloadOut multipart = createHitInstance(
                "entryNumber-" + identifier,
                CURRENT_DATE_UTC,
                currentOwnerRefName,
                depositorRefName,
                conditionCheckerAssessorRefName,
                insurerRefName,
                valuerRefName);

        Response res = hitClient.create(multipart);
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
            res.close();
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownHitId == null) {
            knownHitId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownHitId=" + knownHitId);
            }
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        hitIdsCreated.add(extractId(res));
    }

    /**
     * Creates the person refs.
     * @throws Exception
     */
    protected void createPersonRefs() throws Exception {
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

    protected String createPerson(String firstName, String surName, String shortId, String authRefName) throws Exception {
    	String result = null;

        PersonClient personAuthClient = new PersonClient();
        Map<String, String> personInfo = new HashMap<String, String>();
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
	        result = extractId(res);
        } finally {
        	res.close();
        }

        return result;
    }

    // Success outcomes
    @Test(dataProvider = "testName", dependsOnMethods = {"createHitWithAuthRefs"})
    public void readAndCheckAuthRefDocs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Get the auth ref docs and check them

        PersonClient personAuthClient = new PersonClient();
        Response res = personAuthClient.getReferencingObjects(personAuthCSID, currentOwnerPersonCSID);
        AuthorityRefDocList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.readEntity(AuthorityRefDocList.class);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        List<AuthorityRefDocList.AuthorityRefDocItem> items =
                list.getAuthorityRefDocItem();
        Assert.assertTrue(items != null);
        Assert.assertTrue(items.size() > 0);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        boolean fFoundHit = false;
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
                if (!fFoundHit && knownHitId.equalsIgnoreCase(item.getDocId())) {
                    fFoundHit = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundHit, "Did not find Hit with authref!");
        }
        //
        // Get the referencing objects
        //
        personAuthClient = new PersonClient();
        res = personAuthClient.getReferencingObjects(personAuthCSID, depositorPersonCSID);
        try {
	        assertStatusCode(res, testName);
	        list = res.readEntity(AuthorityRefDocList.class);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        items = list.getAuthorityRefDocItem();
        Assert.assertTrue(items != null);
        Assert.assertTrue(items.size() > 0);
        Assert.assertTrue(items.get(0) != null);

        // Optionally output additional data about list members for debugging.
        iterateThroughList = true;
        fFoundHit = false;
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
                if (!fFoundHit && knownHitId.equalsIgnoreCase(item.getDocId())) {
                    fFoundHit = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundHit, "Did not find Hit with authref!");
        }
    }

    /*
     * Read and check the list of referencing objects, where the authRef field
     * is a value instance of a repeatable scalar field.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createHitWithAuthRefs"},
    		groups = {"repeatableScalar"})
    public void readAndCheckAuthRefDocsRepeatableScalar(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Get the auth ref docs and check them

        // Single scalar field
        PersonClient personAuthClient = new PersonClient();
        Response res = personAuthClient.getReferencingObjects(personAuthCSID, insurerPersonCSID);
        AuthorityRefDocList list = null;
        try {
	        assertStatusCode(res, testName);
	        list = res.readEntity(AuthorityRefDocList.class);
        } finally {
        	if (res != null) {
                res.close();
            }
        }

        List<AuthorityRefDocList.AuthorityRefDocItem> items =
                list.getAuthorityRefDocItem();
        Assert.assertTrue(items != null);
        Assert.assertTrue(items.size() > 0);
        Assert.assertTrue(items.get(0) != null);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        boolean fFoundHit = false;
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
                if (!fFoundHit && knownHitId.equalsIgnoreCase(item.getDocId())) {
                    fFoundHit = true;
                }
                i++;
            }
            Assert.assertTrue(fFoundHit, "Did not find Hit with authref!");
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
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
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
        HitClient hitClient = new HitClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : hitIdsCreated) {
            hitClient.delete(resourceId).close();
        }
        // Delete persons before PersonAuth
        PersonClient personAuthClient = new PersonClient();
        for (String resourceId : personIdsCreated) {
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
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	private PoxPayloadOut createHitInstance(String entryNumber, String entryDate, String currentOwner, String depositor,
			String conditionCheckerAssessor, String insurer, String Valuer) throws Exception {

		HitsCommon hit = HitClientTestUtil.createHitInstance(entryNumber, currentOwner, depositor,
				conditionCheckerAssessor, insurer);
		hit.setHitNumber(entryNumber);

		PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
		PayloadOutputPart commonPart = multipart.addPart(new HitClient().getCommonPartName(), hit);

		if (logger.isDebugEnabled()) {
			logger.debug("to be created, hit common");
			logger.debug(objectAsXmlString(hit, HitsCommon.class));
		}

		return multipart;
	}
}
