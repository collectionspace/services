/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 * <p>
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 * <p>
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 * <p>
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.collectionspace.services.ChronologyJAXBSchema;
import org.collectionspace.services.chronology.ChronologiesCommon;
import org.collectionspace.services.chronology.ChronologyTermGroup;
import org.collectionspace.services.chronology.ChronologyTermGroupList;
import org.collectionspace.services.chronology.ChronologyauthoritiesCommon;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.ChronologyAuthorityClient;
import org.collectionspace.services.client.ChronologyAuthorityClientUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Test against a deployed ChronologyAuthority Service
 */
public class ChronologyAuthorityServiceTest
    extends AbstractAuthorityServiceTest<ChronologyauthoritiesCommon, ChronologiesCommon> {

    private static final Logger logger = LoggerFactory.getLogger(ChronologyAuthorityServiceTest.class);

    private static final String TEST_CHRONOLOGY_DESCRIPTION = "A Chronology description";
    private static final String TEST_CHRONOLOGY_TERM_NAME = "ChronoTerm";
    private static final String TEST_CHRONOLOGY_TERM_DISPLAY_NAME = "Chronology 1";
    private static final String TEST_CHRONOLOGY_TERM_STATUS = "accepted";
    private static final String TEST_CHRONOLOGY_TERM_SOURCE = "source";
    private static final String TEST_CHRONOLOGY_TERM_SOURCE_DETAIL = "detail";

    public ChronologyAuthorityServiceTest() {
        super();
        TEST_SHORTID = "chronology1";
    }

    @Test(dataProvider="testName")
    public void verifyIllegalItemDisplayName(String testName) throws Exception {
        // Perform setup for read
        setupRead();

        // Submit the request to the service and store the response.
        final ChronologyAuthorityClient client = new ChronologyAuthorityClient();
        final Response res = client.readItem(knownResourceId, knownItemResourceId);
        final ChronologiesCommon chronology;
        try {
            assertStatusCode(res, testName);
            final String commonPartName = client.getItemCommonPartName();
            final PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
            chronology = (ChronologiesCommon) extractPart(input, commonPartName, ChronologiesCommon.class);
            assertNotNull(chronology);
        } finally {
            if (res != null) {
                res.close();
            }
        }

        //
        // Make an invalid UPDATE request, without a display name
        //
        ChronologyTermGroupList termList = chronology.getChronologyTermGroupList();
        assertNotNull(termList);
        List<ChronologyTermGroup> terms = termList.getChronologyTermGroup();
        assertNotNull(terms);
        assertTrue(terms.size() > 0);
        terms.get(0).setTermDisplayName(null);
        terms.get(0).setTermName(null);

        // we expect a failure
        setupUpdateWithInvalidBody();

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(ChronologyAuthorityClient.SERVICE_ITEM_NAME);
        output.addPart(client.getItemCommonPartName(), chronology);

        // we expected a failure here.
        setupUpdateWithInvalidBody();
        final Response updateResponse = client.updateItem(knownResourceId, knownItemResourceId, output);
        try {
            assertStatusCode(updateResponse, testName);
        } finally {
            if (updateResponse != null) {
                updateResponse.close();
            }
        }
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"localDeleteItem"})
    public void localDelete(String testName) throws Exception {
        super.delete(testName);
    }

    @Test(dataProvider = "testName", groups = {"delete"}, dependsOnMethods = {"verifyIllegalItemDisplayName"})
    public void localDeleteItem(String testName) throws Exception {
        super.deleteItem(testName);
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        final String noTestCleanup = System.getProperty("noTestCleanup");
        if (Boolean.parseBoolean(noTestCleanup)) {
            logger.debug("Skipping cleanup");
            return;
        }

        logger.debug("Cleaning temporary resources for testing");

        ChronologyAuthorityClient client = new ChronologyAuthorityClient();
        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
            final String itemResourceId = entry.getKey();
            final String parentResourceId = entry.getValue();
            client.deleteItem(parentResourceId, itemResourceId).close();
        }

        for (String resourceId : allResourceIdsCreated) {
            client.delete(resourceId).close();
        }
    }

    @Override
    public void authorityTests(String testName) {
        // empty
    }

    @Override
    public void delete(String testName) {
        // Do nothing. Ensures proper test order.
    }

    @Override
    public void deleteItem(String testName) {
        // Do nothing. Ensures proper test order.
    }

    @Override
    protected String createItemInAuthority(final AuthorityClient client, final String vcsid, final String shortId) {
        return createItemInAuthority(client, vcsid, shortId, null);
    }

    private String createItemInAuthority(final AuthorityClient client,
                                         final String vcsid,
                                         final String shortId,
                                         final String authRefName) {
        final String testName = String.format("createItemInAuthority(%s, %s)", vcsid, shortId);

        Map<String, String> materialMap = new HashMap<>();
        materialMap.put(ChronologyJAXBSchema.SHORT_IDENTIFIER, shortId);
        materialMap.put(ChronologyJAXBSchema.CHRONOLOGY_DESCRIPTION, TEST_CHRONOLOGY_DESCRIPTION);

        List<ChronologyTermGroup> terms = new ArrayList<>();
        ChronologyTermGroup term = new ChronologyTermGroup();
        term.setTermName(TEST_CHRONOLOGY_TERM_NAME);
        term.setTermDisplayName(TEST_CHRONOLOGY_TERM_DISPLAY_NAME);
        term.setTermSource(TEST_CHRONOLOGY_TERM_SOURCE);
        term.setTermSourceDetail(TEST_CHRONOLOGY_TERM_SOURCE_DETAIL);
        term.setTermStatus(TEST_CHRONOLOGY_TERM_STATUS);
        terms.add(term);

        final String id = ChronologyAuthorityClientUtils.createItemInAuthority(vcsid, authRefName, materialMap, terms,
                                                                               (ChronologyAuthorityClient) client);

        // Store the id returned from the first item resource created for additional tests
        if (knownItemResourceId == null) {
            setKnownItemResource(id, shortId);
            logger.debug("{}: knownResourceId={}", testName, id);
        }

        // Store item resource ids and parent ids so that they can be deleted after all tests run
        allResourceItemIdsCreated.put(id, vcsid);

        return id;
    }

    @Override
    protected ChronologiesCommon updateItemInstance(final ChronologiesCommon authorityItem) {
        final ChronologyTermGroupList termList = authorityItem.getChronologyTermGroupList();
        assertNotNull(termList);
        final List<ChronologyTermGroup> terms = termList.getChronologyTermGroup();
        assertNotNull(terms);
        assertTrue(terms.size() > 0);
        terms.get(0).setTermDisplayName("updated-" + terms.get(0).getTermDisplayName());
        terms.get(0).setTermName("updated-" + terms.get(0).getTermName());
        authorityItem.setChronologyTermGroupList(termList);
        return authorityItem;
    }

    @Override
    protected void compareUpdatedItemInstances(final ChronologiesCommon original,
                                               final ChronologiesCommon updated,
                                               final boolean compareRevNumbers) {
        final ChronologyTermGroupList originalTermList = original.getChronologyTermGroupList();
        assertNotNull(originalTermList);
        final List<ChronologyTermGroup> originalTerms = originalTermList.getChronologyTermGroup();
        assertNotNull(originalTerms);
        assertTrue(originalTerms.size() > 0);

        final ChronologyTermGroupList updatedTermList = updated.getChronologyTermGroupList();
        assertNotNull(updatedTermList);
        final List<ChronologyTermGroup> updatedTerms = updatedTermList.getChronologyTermGroup();
        assertNotNull(updatedTerms);
        assertTrue(updatedTerms.size() > 0);

        assertEquals(updatedTerms.get(0).getTermDisplayName(), originalTerms.get(0).getTermDisplayName(),
                     "Value in updated record did not match submitted data.");

        if (compareRevNumbers) {
            assertEquals(original.getRev(), updated.getRev(), "Revision numbers should match.");
        }
    }

    @Override
    protected void verifyReadItemInstance(final ChronologiesCommon item) {
        // empty
    }

    @Override
    protected PoxPayloadOut createNonExistenceInstance(final String commonPartName, final String identifier) {
        final String displayName = "displayName-NON_EXISTENT_ID";
        return ChronologyAuthorityClientUtils.createChronologyAuthorityInstance(displayName, "nonEx", commonPartName);
    }

    @Override
    protected PoxPayloadOut createNonExistenceItemInstance(String commonPartName, String identifier) {
        Map<String, String> nonexMap = new HashMap<>();
        nonexMap.put(ChronologyJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(ChronologyJAXBSchema.TERM_STATUS, TEST_CHRONOLOGY_TERM_STATUS);
        nonexMap.put(ChronologyJAXBSchema.TERM_DISPLAY_NAME, TEST_CHRONOLOGY_TERM_DISPLAY_NAME);
        final List<ChronologyTermGroup> termGroupInstance =
            ChronologyAuthorityClientUtils.getTermGroupInstance(TEST_CHRONOLOGY_DESCRIPTION);

        return ChronologyAuthorityClientUtils.createChronologyInstance(nonexMap, termGroupInstance, commonPartName);
    }

    @Override
    protected PoxPayloadOut createInstance(final String commonPartName, final String identifier) {
        // Submit the request to the service and store the response.
        final String displayName = "displayName-" + identifier;
        return ChronologyAuthorityClientUtils.createChronologyAuthorityInstance(displayName, identifier,
                                                                                commonPartName);
    }

    @Override
    protected ChronologyauthoritiesCommon updateInstance(final ChronologyauthoritiesCommon commonPartObject) {
        ChronologyauthoritiesCommon result = new ChronologyauthoritiesCommon();

        result.setDisplayName("updated-" + commonPartObject.getDisplayName());
        result.setVocabType("updated-" + commonPartObject.getVocabType());

        return result;
    }

    @Override
    protected void compareUpdatedInstances(final ChronologyauthoritiesCommon original,
                                           final ChronologyauthoritiesCommon updated) {
        assertEquals(updated.getDisplayName(), original.getDisplayName(),
                            "Display name in updated object did not match submitted data.");
    }

    @Override
    protected void compareReadInstances(final ChronologyauthoritiesCommon original,
                                        final ChronologyauthoritiesCommon fromRead) {
        assertNotNull(fromRead.getDisplayName());
        assertNotNull(fromRead.getShortIdentifier());
        assertNotNull(fromRead.getRefName());
    }

    @Override
    protected CollectionSpaceClient getClientInstance() throws Exception {
        return new ChronologyAuthorityClient();
    }

    @Override
    protected CollectionSpaceClient getClientInstance(String clientPropertiesFilename) throws Exception {
        return new ChronologyAuthorityClient(clientPropertiesFilename);
    }

    @Override
    protected String getServicePathComponent() {
        return ChronologyAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return ChronologyAuthorityClient.SERVICE_NAME;
    }

    @Override
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + AuthorityClient.ITEMS;
    }

    @Override
    protected String getItemResourceURL(String parentResourceIdentifier, String resourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + resourceIdentifier;
    }
}
