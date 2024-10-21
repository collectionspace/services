/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 * <p>
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 * <p>
 * Copyright © 2009 Regents of the University of California
 * <p>
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 * <p>
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 * <p>
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
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RestrictedMediaClient;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.person.PersonTermGroup;
import org.collectionspace.services.restrictedmedia.RestrictedMediaCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * MediaAuthRefsTest, carries out Authority References tests against a deployed and running Media (aka Loans Out) Service.
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class RestrictedMediaAuthRefsTest extends BaseServiceTest<AbstractCommonList> {
    private final Logger logger = LoggerFactory.getLogger(RestrictedMediaAuthRefsTest.class);
    final String PERSON_AUTHORITY_NAME = "MediaPersonAuth";
    private String knownResourceId = null;
    private List<String> mediaIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String depositorRefName = null;
    private String title = null;

    @Override
    public String getServicePathComponent() {
        return RestrictedMediaClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return RestrictedMediaClient.SERVICE_NAME;
    }

    @Override
    protected CollectionSpaceClient getClientInstance() {
        throw new UnsupportedOperationException(); // method not supported (or needed) in this test class
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) {
        throw new UnsupportedOperationException(); // method not supported (or needed) in this test class
    }

    @Override
    protected AbstractCommonList getCommonList(Response response) {
        throw new UnsupportedOperationException(); // method not supported (or needed) in this test class
    }

    private PoxPayloadOut createMediaInstance(String depositorRefName, String title) throws Exception {
        this.title = title;
        this.depositorRefName = depositorRefName;
        RestrictedMediaCommon media = new RestrictedMediaCommon();
        media.setTitle(title);

        PoxPayloadOut multipart = new PoxPayloadOut(RestrictedMediaClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(new RestrictedMediaClient().getCommonPartName(), media);
        logger.debug("to be created, media common: " + objectAsXmlString(media, RestrictedMediaCommon.class));
        return multipart;
    }

    @Test(dataProvider = "testName")
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        String identifier = createIdentifier(); // Submit the request to the service and store the response.
        createPersonRefs(); // Create all the person refs and entities
        // Create a new Loans In resource. One or more fields in this resource will be PersonAuthority
        //    references, and will refer to Person resources by their refNames.
        RestrictedMediaClient mediaClient = new RestrictedMediaClient();
        PoxPayloadOut multipart = createMediaInstance(depositorRefName, "media.title-" + identifier);
        Response res = mediaClient.create(multipart);
        try {
            assertStatusCode(res, testName);
            // Store the ID returned from the first resource created for additional tests below.
            if (knownResourceId == null) {
                knownResourceId = extractId(res);
            }
            // Store the IDs from every resource created; delete on cleanup
            mediaIdsCreated.add(extractId(res));
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    protected void createPersonRefs() throws Exception {
        PersonClient personAuthClient = new PersonClient();
        // Create a temporary PersonAuthority resource, and its corresponding refName by which it can be identified.
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
                PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        Response res = personAuthClient.create(multipart);
        try {
            assertStatusCode(res, "createPersonRefs (not a surefire test)");
            personAuthCSID = extractId(res);
        } finally {
            if (res != null) {
                res.close();
            }
        }
        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
        // Create temporary Person resources, and their corresponding refNames by which they can be identified.
        String csid = "";

        csid = createPerson("Owen the Cur", "Owner", "owenCurOwner", authRefName);
        personIdsCreated.add(csid);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Davenport", "Depositor", "davenportDepositor", authRefName);
        personIdsCreated.add(csid);
        depositorRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
    }

    protected String createPerson(String firstName, String surName, String shortId, String authRefName)
            throws Exception {
        String result = null;

        PersonClient personAuthClient = new PersonClient();
        Map<String, String> personInfo = new HashMap<String, String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId + random.nextInt(1000));
        List<PersonTermGroup> personTerms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        String termName = firstName + " " + surName;
        term.setTermDisplayName(termName);
        term.setTermName(termName);
        personTerms.add(term);
        PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonInstance(
                personAuthCSID, authRefName, personInfo, personTerms, personAuthClient.getItemCommonPartName());
        Response res = personAuthClient.createItem(personAuthCSID, multipart);
        try {
            assertStatusCode(res, "createPerson (not a surefire test)");
            result = extractId(res);
        } finally {
            if (res != null) {
                res.close();
            }
        }

        return result;
    }

    // @Test annotation commented out by Aron 2010-12-02 until media payload is set to the
    // actual payload - it currently appears to be cloned from another record type - and
    // it's determined that this payload has at least one authority field.
    //
    // When that happens, this test class will also need to be revised accordingly to
    // reflect the actual names and number of authref fields in that payload.
    //
    // @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods =
    // {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {
        testSetup(STATUS_OK, ServiceRequestType.READ);

        RestrictedMediaClient mediaClient = new RestrictedMediaClient();
        Response res = mediaClient.read(knownResourceId);
        PoxPayloadIn input = null;
        RestrictedMediaCommon media = null;
        try {
            assertStatusCode(res, testName);
            input = new PoxPayloadIn(res.readEntity(String.class));
            media = (RestrictedMediaCommon)
                    extractPart(input, mediaClient.getCommonPartName(), RestrictedMediaCommon.class);
            Assert.assertNotNull(media);
            logger.debug(objectAsXmlString(media, RestrictedMediaCommon.class));
        } finally {
            if (res != null) {
                res.close();
            }
        }

        // Check a couple of fields
        Assert.assertEquals(media.getTitle(), title);

        // Get the auth refs and check them
        Response res2 = mediaClient.getAuthorityRefs(knownResourceId);
        AuthorityRefList list = null;
        try {
            assertStatusCode(res2, testName);
            list = res2.readEntity(AuthorityRefList.class);
        } finally {
            if (res2 != null) {
                res2.close();
            }
        }

        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        logger.debug("Authority references, found " + numAuthRefsFound);
        // Assert.assertEquals(numAuthRefsFound, NUM_AUTH_REFS_EXPECTED,
        //                    "Did not find all expected authority references! " +
        //                    "Expected " + NUM_AUTH_REFS_EXPECTED + ", found " + numAuthRefsFound);
        if (logger.isDebugEnabled()) {
            int i = 0;
            for (AuthorityRefList.AuthorityRefItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] Field:" + item.getSourceField() + "= "
                        + item.getAuthDisplayName() + item.getItemDisplayName());
                logger.debug(testName + ": list-item[" + i + "] refName=" + item.getRefName());
                logger.debug(testName + ": list-item[" + i + "] URI=" + item.getUri());
                i++;
            }
        }
    }

    /**
     * Deletes all resources created by tests, after all tests have been run.
     * <p/>
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        String noTestCleanup = System.getProperty("noTestCleanup");
        if (Boolean.parseBoolean(noTestCleanup)) {
            logger.debug("Skipping Cleanup phase ...");
            return;
        }
        logger.debug("Cleaning up temporary resources created for testing ...");
        PersonClient personAuthClient = new PersonClient();
        // Delete Person resource(s) (before PersonAuthority resources).
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId).close();
        }
        // Delete PersonAuthority resource(s).
        // Note: Any non-success response is ignored and not reported.
        if (personAuthCSID != null) {
            personAuthClient.delete(personAuthCSID);
            // Delete Loans In resource(s).
            RestrictedMediaClient mediaClient = new RestrictedMediaClient();
            for (String resourceId : mediaIdsCreated) {
                // Note: Any non-success responses are ignored and not reported.
                mediaClient.delete(resourceId);
            }
        }
    }
}
