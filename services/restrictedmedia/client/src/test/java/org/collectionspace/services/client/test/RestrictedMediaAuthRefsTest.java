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
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PersonClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RestrictedMediaClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.person.PersonTermGroup;
import org.collectionspace.services.restrictedmedia.RestrictedMediaCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * RestrictedMediaAuthRefsTest, carries out Authority References tests against a deployed and running
 * Restricted Media Service.
 */
public class RestrictedMediaAuthRefsTest extends BaseServiceTest<AbstractCommonList> {
    private final Logger logger = LoggerFactory.getLogger(RestrictedMediaAuthRefsTest.class);
    final String PERSON_AUTHORITY_NAME = "MediaPersonAuth";
    private String knownResourceId = null;
    private List<String> mediaIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String publisherRefName = null;

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
        throw new UnsupportedOperationException();
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected AbstractCommonList getCommonList(Response response) {
        throw new UnsupportedOperationException();
    }

    private PoxPayloadOut createMediaInstance(String depositorRefName, String title) throws Exception {
        RestrictedMediaCommon media = new RestrictedMediaCommon();
        media.setIdentificationNumber(UUID.randomUUID().toString());
        media.setTitle(title);
        media.setPublisher(depositorRefName);

        PoxPayloadOut multipart = new PoxPayloadOut(RestrictedMediaClient.SERVICE_PAYLOAD_NAME);
        multipart.addPart(new RestrictedMediaClient().getCommonPartName(), media);
        logger.debug("to be created, media common: {}", objectAsXmlString(media, RestrictedMediaCommon.class));
        return multipart;
    }

    @Test(dataProvider = "testName")
    public void createWithAuthRefs(String testName) throws Exception {
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);
        String identifier = createIdentifier();

        createPersonRefs();

        // Create a new RestrictedMedia resource.
        // One or more fields in this resource will be PersonAuthority references
        // and will refer to Person resources by their refNames.
        RestrictedMediaClient mediaClient = new RestrictedMediaClient();
        PoxPayloadOut multipart = createMediaInstance(publisherRefName, "media.title-" + identifier);
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
        assertStatusCode(res, "createPersonRefs (not a surefire test)");
        personAuthCSID = extractId(res);

        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);

        String csid = createPerson("Owen the Cur", "Owner", "owenCurOwner", authRefName);
        personIdsCreated.add(csid);
        publisherRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
    }

    protected String createPerson(String firstName, String surName, String shortId, String authRefName)
            throws Exception {
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
        assertStatusCode(res, "createPerson (not a surefire test)");
        return extractId(res);
    }

    /**
     * Deletes all resources created by tests, after all tests have been run.
     * <p/>
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests. Non-successful deletes are ignored and not reported
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        String noTestCleanup = System.getProperty("noTestCleanup");
        if (Boolean.parseBoolean(noTestCleanup)) {
            logger.debug("Skipping Cleanup phase...");
            return;
        }
        logger.debug("Cleaning up temporary resources created for testing...");
        PersonClient personAuthClient = new PersonClient();
        // Delete Person resource(s) (before PersonAuthority resources).
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
            personAuthClient.deleteItem(personAuthCSID, resourceId);
        }

        if (personAuthCSID != null) {
            personAuthClient.delete(personAuthCSID);
        }

        RestrictedMediaClient mediaClient = new RestrictedMediaClient();
        for (String resourceId : mediaIdsCreated) {
            mediaClient.delete(resourceId);
        }
    }
}
