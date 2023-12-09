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
import org.collectionspace.services.client.HeldintrustClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.heldintrust.HeldintrustsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.person.PersonTermGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * HitAuthRefsTest, carries out tests against a
 * deployed and running Hit Service.
 *
 * $LastChangedRevision: 1327 $
 * $LastChangedDate: 2010-02-12 10:35:11 -0800 (Fri, 12 Feb 2010) $
 */
public class HeldintrustAuthRefsTest extends BaseServiceTest<AbstractCommonList> {

    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = HeldintrustClient.SERVICE_PATH_COMPONENT;
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private final String CLASS_NAME = HeldintrustAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    private final List<String> hitIdsCreated = new ArrayList<String>();
    private final List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String depositorContactRefName = null;
    private String depositorRefName = null;
    private String externalApprovalIndividualRefName = null;
    private String correspondenceSenderRefName = null;
    private String valuerRefName = null;

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

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client
     * .ClientResponse)
     */
    @Override
    protected AbstractCommonList getCommonList(Response response) {
        throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();

        // Create all the person refs and entities
        createPersonRefs();

        // Submit the request to the service and store the response.
        HeldintrustClient heldInTrustClient = new HeldintrustClient();
        PoxPayloadOut multipart = createHitInstance(
            "entryNumber-" + identifier,
            depositorContactRefName,
            depositorRefName,
            externalApprovalIndividualRefName,
            correspondenceSenderRefName);

        String newId;
        Response res = heldInTrustClient.create(multipart);
        try {
            int statusCode = res.getStatus();
            // Check the status code of the response: does it match
            // the expected response(s)?
            //
            // Specifically:
            // Does it fall within the set of valid status codes?
            // Does it exactly match the expected status code?
            logger.debug("{}: status = {}", testName, statusCode);
            Assert.assertTrue(testRequestType.isValidStatusCode(statusCode),
                              invalidStatusCodeMessage(testRequestType, statusCode));
            Assert.assertEquals(statusCode, testExpectedStatusCode);
            newId = extractId(res);
        } finally {
            res.close();
        }

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null) {
            knownResourceId = newId;
            logger.debug("{}: knownResourceId={}", testName, knownResourceId);
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        hitIdsCreated.add(newId);
    }

    protected void createPersonRefs() throws Exception {
        //
        // First, create a new person authority
        //
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
        String csid = createPerson("Olivier", "Contact", "olivierContact", authRefName);
        depositorContactRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Debbie", "Depositor", "debbieDepositor", authRefName);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Andrew", "ApprovalIndividual", "andrewApprovalIndividual", authRefName);
        externalApprovalIndividualRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Ingrid", "Sender", "ingridSender", authRefName);
        correspondenceSenderRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);

        csid = createPerson("Vince", "Valuer", "vinceValuer", authRefName);
        valuerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        personIdsCreated.add(csid);
    }

    protected String createPerson(String firstName, String surName, String shortId, String authRefName)
        throws Exception {
        String result;

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
                                                            authRefName, personInfo, personTerms,
                                                            personAuthClient.getItemCommonPartName());

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
    @Test(dataProvider = "testName", dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        HeldintrustClient heldInTrustClient = new HeldintrustClient();
        Response res = heldInTrustClient.read(knownResourceId);
        try {
            assertStatusCode(res, testName);
            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
            HeldintrustsCommon hit = (HeldintrustsCommon) extractPart(input, heldInTrustClient.getCommonPartName(),
                                                                      HeldintrustsCommon.class);
            Assert.assertNotNull(hit);
            // Check a couple of fields
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Get the auth refs and check them
        res = heldInTrustClient.getAuthorityRefs(knownResourceId);
        AuthorityRefList list;
        try {
            assertStatusCode(res, testName);
            list = res.readEntity(AuthorityRefList.class);
        } finally {
            if (res != null) {
                res.close();
            }
        }

        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        logger.debug("Expected {} authority references, found {}", personIdsCreated.size(), numAuthRefsFound);

        // output additional data about list members for debugging.
        int i = 0;
        for (AuthorityRefList.AuthorityRefItem item : items) {
            logger.debug("{}: list-item[{}] Field:{}={}{}", testName, i, item.getSourceField(),
                         item.getAuthDisplayName(), item.getItemDisplayName());
            logger.debug("{}: list-item[{}] refName={}", testName, i, item.getRefName());
            logger.debug("{}: list-item[{}] URI={}", testName, i, item.getUri());
            i++;
        }

        //
        // Ensure we got the correct number of authRefs
        Assert.assertEquals(numAuthRefsFound, personIdsCreated.size(),
                            "Did not find all expected authority references! " + "Expected " + personIdsCreated.size() +
                            ", found " + numAuthRefsFound);
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
     *
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.parseBoolean(noTest)) {
            logger.debug("Skipping Cleanup phase ...");
            return;
        }

        logger.debug("Cleaning up temporary resources created for testing ...");
        HeldintrustClient heldInTrustClient = new HeldintrustClient();
        // Note: Any non-success responses are ignored and not reported.
        for (String resourceId : hitIdsCreated) {
            heldInTrustClient.delete(resourceId).close();
        }
        //
        // Delete all the person records then the parent resource
        PersonClient personAuthClient = new PersonClient();
        for (String resourceId : personIdsCreated) {
            personAuthClient.deleteItem(personAuthCSID, resourceId).close();
        }
        personAuthClient.delete(personAuthCSID).close();
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private PoxPayloadOut createHitInstance(String heldInTrustNumber,
                                            String depositorContact,
                                            String depositor,
                                            String externalApprovalIndividual,
                                            String correspondenceSender) throws Exception {
        HeldintrustsCommon hit =
            HeldintrustClientTestUtil.createHitInstance(heldInTrustNumber, depositorContact, depositor,
                                                        externalApprovalIndividual, correspondenceSender);
        hit.setHeldInTrustNumber(heldInTrustNumber);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        multipart.addPart(new HeldintrustClient().getCommonPartName(), hit);

        logger.debug("to be created, hit common");
        logger.debug("{}", objectAsXmlString(hit, HeldintrustsCommon.class));

        return multipart;
    }
}
